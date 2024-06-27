package org.pseudosweep.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import javassist.bytecode.ClassFile;
import org.pseudosweep.DataManager;
import org.pseudosweep.Sweeper;
import org.pseudosweep.TestMethodReport;
import org.pseudosweep.logging.LogManager;
import org.pseudosweep.logging.Logger;
import org.pseudosweep.PseudoSweepException;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.pseudosweep.util.StringUtils.*;

@Parameters(commandDescription = " >>> finds the effectual coverage of a test suite")
public class SweepCommand extends ListTestClassesCommand {
    private String operatorSet;

    private static final Logger logger = LogManager.getLogger(SweepCommand.class);

    @Parameter(names = {"-m", "--testMethodNames"}, variableArity = true,
            description = "A space separated list of test methods of the test class to investigate (if not specified, all test methods are used.")
    Set<String> testMethodNames = new HashSet<>();

    @Parameter(names = {"-r", "--testRepeats"},
            description = "The number of times to repeat tests to mitigate against the possibility of non-determinism.")
    int testRepeats = 3;

    @Parameter(names = {"-t", "--testTimeout"},
            description = "The timeout (in milliseconds) for long-running tests.")
    long testTimeout = 60000;

    @Override
    public String getName() {
        return "sweep";
    }

    @Override
    public void checkParams() throws ParameterException {
        super.checkParams();
        if (isNullOrEmpty(testClassFileName) && !testMethodNames.isEmpty()) {
            throw new ParameterException("--testMethodNames (-m) can only be used in conjunction with --testClassFileName (-f)");
        }
        if (!(statementDeletion || extremeMutation)) {
            throw new ParameterException("One of --statementdeletion (-sdl) or --extrememutation (-xmt) must be used");
        }
        operatorSet = (statementDeletion) ? "sdl" : "xmt";
    }

    @Override
    void run() {
        List<String> testClassFileNames = getTestClassFileNames();
        int fileNo = 0, numFiles = testClassFileNames.size();

        for (String testClassFileName : testClassFileNames) {
            try {
                fileNo++;
                processTestClassFile(fileNo, numFiles, testClassFileName, operatorSet);
            } catch (ClassNotFoundException | IOException e) {
                throw new PseudoSweepException(e);
            }
        }
    }

    private void processTestClassFile(int fileNo, int numFiles, String testClassFileName, String operatorSet) throws ClassNotFoundException, IOException {
        String testClassName = getClassName(testClassFileName);
        System.out.println(testClassFileName);
        Class<?> testClass;
        try {
            testClass = Class.forName(testClassName);
        } catch (Exception e) {
            return;
        }

        boolean resultsAlreadyExist = DataManager.testClassResultsDirectoryExists(testClassName, operatorSet);

        if (!resultsAlreadyExist) {
            DataManager.createTestClassResultsDirectory(testClassName, operatorSet);
            DataManager.startTestClassLogging(testClassName, operatorSet);

        }

        logger.info(fileNo + "/" + numFiles + ": " +
                getShortPath(testClassFileName) + " " + encloseParentheses(testClassName));

        if (resultsAlreadyExist) {
            logger.info("... is already done or in progress");
            return;
        }

        if (Modifier.isAbstract(testClass.getModifiers())) {
            logger.info("... is an abstract class (ignoring)");
            return;
        }

        coverageSweeper(testClass);

        DataManager.endTestClassLogging();
    }

    private void coverageSweeper(Class<?> testClass) {
        Set<Method> testMethods = getTestClassMethods(testClass);
        for (Method testMethod : testMethods) {
            Sweeper sweeper = new Sweeper(testRepeats, testTimeout);
            TestMethodReport testMethodReport = sweeper.sweep(testMethod, testClass);

            if (testMethodReport != null) {
                DataManager.serializeTestMethodReport(testMethodReport, operatorSet);
            }
        }
    }

    String getClassName(String pathToClassFile) throws IOException {
        ClassFile cf = new ClassFile(new DataInputStream(new FileInputStream(pathToClassFile)));
        return cf.getName();
    }

    private String getShortPath(String testClassFileName) {
        return (isNullOrEmpty(testClassPath) ?
                testClassFileName :
                testClassFileName.replace(testClassPath, ""));
    }

    Set<Method> getTestClassMethods(Class<?> testClass) {
        Set<Method> testClassMethods = new HashSet<>();

        if (!isNullOrEmpty(testClassFileName) && !testMethodNames.isEmpty()) {
            for (String testMethodName : testMethodNames) {
                try {
                    testClassMethods.add(testClass.getMethod(testMethodName));
                } catch (NoSuchMethodException e) {
                    throw new PseudoSweepException("No such method " + encloseQuotes(testMethodName) +
                            " in class " + encloseQuotes(testClass.getCanonicalName()));
                }
            }
        } else {
            testClassMethods.addAll(Arrays.asList(testClass.getMethods()));
        }

        return testClassMethods;
    }
}

