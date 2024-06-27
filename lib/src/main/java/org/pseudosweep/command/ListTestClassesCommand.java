package org.pseudosweep.command;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import org.pseudosweep.PseudoSweepException;
import org.pseudosweep.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.pseudosweep.util.StringUtils.encloseQuotes;
import static org.pseudosweep.util.StringUtils.isNullOrEmpty;

@Parameters(commandDescription = " >>> lists Java classes present in a directory (useful for composing scripts for running PseudoSweep")
public class ListTestClassesCommand extends Command {

    @Parameter(names = {"-p", "--path"}, description = "The path to the test classes.")
    String testClassPath;

    @Parameter(names = {"-nr", "--noRecurse"}, description = "Do not recurse through subdirectories (if using --path option).")
    boolean noRecurse = false;

    @Parameter(names = {"-f", "--file"}, description = "The file name of the test class to measure coverage for.")
    String testClassFileName;

    @Parameter(names = {"-i", "--ignoreFile"}, description = "A text file of test classes to exclude, one per line")
    String ignoreFileName;

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public void checkParams() throws ParameterException {
        if (isNullOrEmpty(testClassFileName) && isNullOrEmpty(testClassPath)) {
            throw new ParameterException("One of --testClassFileName (-f) or --testClassDirectory (-d) needs to be set");
        }
        if (!isNullOrEmpty(testClassFileName) && !isNullOrEmpty(testClassPath)) {
            throw new ParameterException("Use only one of --testClassFileName (-f) or --testClassDirectory (-d)");
        }
    }

    @Override
    void run() {
        for (String testClassFileName : getTestClassFileNames()) {
            System.out.println(testClassFileName);
        }
    }

    List<String> getTestClassFileNames() {
        if (!isNullOrEmpty(testClassFileName)) {
            return Collections.singletonList(testClassFileName);
        } else {
            try {
                List<String> files = FileUtils.getJavaClassFiles(testClassPath, !noRecurse);
                files.removeIf(s -> s.contains("$")); // remove inner classes from consideration

                List<String> removeFiles = getTestClassFileNamesToIgnore();
                files.removeIf(removeFiles::contains);

                Collections.sort(files);

                return files;
            } catch (IOException e) {
                throw new PseudoSweepException("Could not find path " + encloseQuotes(testClassPath));
            }
        }
    }

    List<String> getTestClassFileNamesToIgnore() {
        if (isNullOrEmpty(ignoreFileName)) {
            return new ArrayList<>();
        }

        File file = new File(ignoreFileName);
        if (!file.exists()) {
            throw new PseudoSweepException("Ignore file " + encloseQuotes(ignoreFileName) + " does not exist");
        }
        try {
            return Files.readAllLines(file.toPath(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new PseudoSweepException("Could not read contents of ignore file " + encloseQuotes(ignoreFileName));
        }
    }
}

