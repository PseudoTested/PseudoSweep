package org.pseudosweep;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.pseudosweep.instrumentation.xmt.SourceFileInstrumenter;
import org.pseudosweep.logging.LogManager;
import org.pseudosweep.logging.Logger;
import org.pseudosweep.program.ClassUnderTest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.pseudosweep.util.StringUtils.encloseQuotes;

public class Instrumenter {

    private final boolean sdl;
    private final boolean xmt;
    private final boolean skipTrivial;
    private String operatorSet;

    public Instrumenter(boolean sdl, boolean xmt, boolean skipTrivial) {
        if (sdl) {
            this.operatorSet = "sdl";
        } else if (xmt) {
            this.operatorSet = "xmt";
        }
        this.sdl = sdl;
        this.xmt = xmt;
        this.skipTrivial = skipTrivial;
    }


    private static final Logger logger = LogManager.getLogger(Instrumenter.class);
    private static final String BACKUP_EXTENSION = ".orig";

    public Set<ClassUnderTest> instrument(String fileName) {
        logger.info("Instrumenting " + encloseQuotes(fileName));

        Path javaFile = Paths.get(fileName);
        handleBackup(javaFile);

        CompilationUnit compilationUnit;
        try {
            compilationUnit = StaticJavaParser.parse(javaFile);
        } catch (IOException e) {
            logger.fatal("Could not read source code in " + encloseQuotes(javaFile) + ".");
            return null;
        }

        org.pseudosweep.instrumentation.SourceFileInstrumenter
                sourceFileInstrumenter = getSourceFileInstrumenter(fileName, compilationUnit);

        String instrumented;
        try {
            instrumented = sourceFileInstrumenter.instrument();
            for (String warning : sourceFileInstrumenter.getWarnings()) {
                logger.warn(warning);
            }
        } catch (NullPointerException e) {
            logger.fatal("Invalid instrumenter set, see parameters list for valid options");
            throw new PseudoSweepException(e);
        }

        try {
            BufferedWriter bw = Files.newBufferedWriter(javaFile);
            bw.write(instrumented);
            bw.close();
        } catch (IOException e) {
            logger.fatal("Could not write instrumented source code to " + encloseQuotes(javaFile) + ".");
            throw new PseudoSweepException(e);
        }

        return sourceFileInstrumenter.getClassesParsed();
    }

    private org.pseudosweep.instrumentation.SourceFileInstrumenter getSourceFileInstrumenter(String fileName,
                                                                                              CompilationUnit compilationUnit) {
        if (sdl) {
            return
                    new org.pseudosweep.instrumentation.sdl.SourceFileInstrumenter(fileName,
                                                                                    compilationUnit,
                                                                                    this.operatorSet,
                                                                                    this.skipTrivial);
        } else if (xmt) {
            return
                    new SourceFileInstrumenter(fileName, compilationUnit, this.operatorSet, this.skipTrivial);

        }
        return null;
    }

    public void restore(String fileName) {
        logger.info("Restoring " + encloseQuotes(fileName));

        Path originalFile = Paths.get(fileName);
        Path backupFile = Paths.get(originalFile + BACKUP_EXTENSION);

        if (Files.exists(backupFile)) {
            // if the backup file containing the original code exists, replace javaFile
            // with its contents, so that the instrumented code is not re-instrumented
            try {
                Files.copy(backupFile, originalFile, REPLACE_EXISTING);
                Files.delete(backupFile);
            } catch (IOException e) {
                logger.fatal("Could not restore backup to original source file when copying " +
                        encloseQuotes(backupFile) + " to " + encloseQuotes(originalFile) + ".");
                throw new PseudoSweepException(e);
            }
        }
    }

    private void handleBackup(Path originalFile) {
        Path backupFile = Paths.get(originalFile.toString() + BACKUP_EXTENSION);

        try {
            if (Files.exists(backupFile)) {
                // if the backup file containing the original code exists, replace javaFile
                // with its contents, so that the instrumented code is not re-instrumented
                Files.copy(backupFile, originalFile, REPLACE_EXISTING);
            } else {
                // else copy the original javaFile to a fresh backupFile
                Files.copy(originalFile, backupFile, REPLACE_EXISTING);
            }
        } catch (IOException e) {
            logger.fatal("Could not overwrite original source file with backup when copying " +
                    encloseQuotes(backupFile) + " to " + encloseQuotes(originalFile) + ". " + e.getMessage());
            throw new PseudoSweepException(e);
        }
    }
}
