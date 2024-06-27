package org.pseudosweep.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import org.pseudosweep.DataManager;
import org.pseudosweep.Instrumenter;
import org.pseudosweep.program.ClassUnderTest;
import org.pseudosweep.program.SourceFilesUnderTest;
import org.pseudosweep.PseudoSweepException;
import org.pseudosweep.util.FileUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.pseudosweep.util.StringUtils.encloseQuotes;
import static org.pseudosweep.util.StringUtils.isNullOrEmpty;

@Parameters(commandDescription = " >>> instruments Java source files for sweeper of effectual coverage")
public class InstrumentCommand extends Command {

    String operatorSet = "";

    @Parameter(names = {"-p", "--path"}, description = "The path of the directory containing the source files to instrument.")
    String path;

    @Parameter(names = {"-f", "--file"}, description = "The path to the file to instrument.")
    String pathToFile;

    @Parameter(names = {"-nr", "--norecurse"}, description = "Do not recurse through subdirectories (if using --path option).")
    boolean noRecurse = false;

    @Parameter(names = {"-st", "--skiptrivial"}, description = "Skip instrumentation of 'trivial' methods e.g. get, set and toString methods.")
    boolean skipTrivial = false;

    @Override
    public String getName() {
        return "instrument";
    }

    @Override
    public void checkParams() throws ParameterException {
        if (isNullOrEmpty(pathToFile) && isNullOrEmpty(path)) {
            throw new ParameterException("One of --file (-f) or --dir (-d) needs to be set");
        }
        if (!isNullOrEmpty(pathToFile) && !isNullOrEmpty(path)) {
            throw new ParameterException("Use only one of --file (-f) or --dir (-d)");
        }

        if (!(statementDeletion || extremeMutation)) {
            throw new ParameterException("One of --statementdeletion (-sdl) or --extrememutation (-xmt) must be used");
        }
        operatorSet = (statementDeletion) ? "sdl" : "xmt";
    }

    @Override
    void run() {
        Instrumenter instrumenter = new Instrumenter(statementDeletion, extremeMutation, skipTrivial);
        SourceFilesUnderTest sourceFiles = new SourceFilesUnderTest();
        for (String fileName : getJavaFileNames()) {
            Set<ClassUnderTest> classes = instrumenter.instrument(fileName);
            sourceFiles.addClasses(fileName, classes);
            for (ClassUnderTest classUnderTest : classes) {
                DataManager.serializeClassUnderTest(classUnderTest, operatorSet);
            }
        }
        DataManager.serializeSourceFilesUnderTest(sourceFiles);
    }

    List<String> getJavaFileNames() {
        if (!isNullOrEmpty(pathToFile)) {
            return Collections.singletonList(pathToFile);
        } else {
            try {
                List<String> files = FileUtils.getJavaSourceFiles(path, !noRecurse);
                Collections.sort(files);
                return files;
            } catch (IOException e) {
                throw new PseudoSweepException("Could not find path " + encloseQuotes(path));
            }
        }
    }
}

