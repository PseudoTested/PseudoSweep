package org.pseudosweep;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.pseudosweep.analysis.report.AnalysisReport;
import org.pseudosweep.analysis.report.ClassAnalysisReport;
import org.pseudosweep.logging.ConsoleAppender;
import org.pseudosweep.logging.FileAppender;
import org.pseudosweep.logging.LogLevel;
import org.pseudosweep.logging.LogManager;
import org.pseudosweep.logging.formatter.*;
import org.pseudosweep.program.ClassUnderTest;
import org.pseudosweep.program.SourceFilesUnderTest;
import org.pseudosweep.util.JavaConstants;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.pseudosweep.util.StringUtils.ensureEndsWith;
import static org.pseudosweep.util.StringUtils.join;

public class DataManager {

    private static final String CONSOLE_APPENDER_NAME = "console",
            MAIN_FILE_APPENDER_NAME = "main_file",
            TEST_CLASS_APPENDER_NAME = "test_class";
    private static final String LOG_FILE_EXTENSION = ".log", SERIALIZE_FILE_EXTENSION = ".json";

    private static final String CLASSES_DIRECTORY_NAME = "classes-",
            RESULTS_DIRECTORY_NAME = "results-",
            ANALYSIS_DIRECTORY_NAME = "analysis-";

    private static final String SWEEP_LOG_FILE_NAME = "sweep.log",
            SOURCE_FILES_FILE_NAME = "source-files";

    private static final String DATE_TIME_LOG_FORMAT = "HH:mm:ss:SSS";

    private static String dataPath;


    /*
     * DIRECTORY HANDLING METHODS
     *
     */

    public static void createDataDirectory(String dataPath) throws LaunchException {
        DataManager.dataPath = ensureEndsWith(dataPath, File.separator);
        try {
            Files.createDirectories(Path.of(DataManager.dataPath));
        } catch (IOException e) {
            throw new LaunchException(e);
        }
    }

    public static void createTestClassResultsDirectory(String testClassName, String operatorSet) {
        try {
            Files.createDirectories(Path.of(getTestClassLevelResultsDirectory(testClassName, operatorSet)));
        } catch (IOException e) {
            throw new PseudoSweepException(e);
        }
    }

    private static String getClassAnalysisDirectory(String className, String operatorSet) {
        return join(File.separator,
                dataPath,
                ANALYSIS_DIRECTORY_NAME + operatorSet,
                className.replace(JavaConstants.PACKAGE_SEPARATOR, File.separator));
    }

    public static boolean testClassResultsDirectoryExists(String testClassName, String operatorSet) {
        return new File(getTestClassLevelResultsDirectory(testClassName, operatorSet)).exists();
    }

    private static String getTestClassLevelResultsDirectory(String testClassName, String operatorSet) {
        return join(File.separator,
                dataPath,
                RESULTS_DIRECTORY_NAME + operatorSet,
                testClassName.replace(JavaConstants.PACKAGE_SEPARATOR, File.separator));
    }

    private static String getPackageLevelResultsDirectory(String packageName, String operatorSet) {
        return join(File.separator,
                dataPath,
                CLASSES_DIRECTORY_NAME + operatorSet,
                packageName.replace(JavaConstants.PACKAGE_SEPARATOR, File.separator));
    }

    /*
     * LOG HANDLING METHODS
     *
     */

    public static void configureLogging(String commandName) throws LaunchException {
        try {
            ConsoleAppender consoleAppender = new ConsoleAppender(LogLevel.INFO, getConsoleFormatter());
            FileAppender fileAppender = new FileAppender(getCommandLogFileName(commandName), LogLevel.INFO, getFileFormatter());
            LogManager.addAppender(CONSOLE_APPENDER_NAME, consoleAppender);
            LogManager.addAppender(MAIN_FILE_APPENDER_NAME, fileAppender);
        } catch (IOException e) {
            throw new LaunchException(e);
        }
    }

    public static void startTestClassLogging(String testClassName, String operatorSet) {
        try {
            FileAppender fileAppender = new FileAppender(getTestClassLogFileName(testClassName, operatorSet),
                                                            LogLevel.ALL,
                                                            getFileFormatter());
            LogManager.addAppender(TEST_CLASS_APPENDER_NAME, fileAppender);
        } catch (IOException e) {
            throw new PseudoSweepException(e);
        }
    }

    public static void endTestClassLogging() {
        LogManager.removeAppender(TEST_CLASS_APPENDER_NAME);
    }

    private static Formatter getConsoleFormatter() {
        return new Colorizer(
                new Separator(" ",
                        new DateTimeWriter(DateTimeFormatter.ofPattern(DATE_TIME_LOG_FORMAT)),
                        new EncloseSquareBrackets(new LogLevelWriter()),
                        new MessageWriter()
                ));
    }

    private static Formatter getFileFormatter() {
        return new Separator(" ",
                new DateTimeWriter(DateTimeFormatter.ofPattern(DATE_TIME_LOG_FORMAT)),
                new EncloseSquareBrackets(new ThreadInfoWriter()),
                new LogLevelWriter(),
                new ClassNameWriter(),
                new MessageWriter());
    }

    private static String getCommandLogFileName(String command) {
        return dataPath + File.separator + command + LOG_FILE_EXTENSION;
    }

    private static String getTestClassLogFileName(String testClassName, String operatorSet) {
        return getTestClassLevelResultsDirectory(testClassName, operatorSet) + File.separator + SWEEP_LOG_FILE_NAME;
    }

    /*
     * SERIALIZATION METHODS
     *
     */

    public static void serializeSourceFilesUnderTest(SourceFilesUnderTest sourceFiles) {
        serialize(dataPath, SOURCE_FILES_FILE_NAME, sourceFiles);
    }

    public static void serializeClassUnderTest(ClassUnderTest classUnderTest, String operatorSet) {
        serialize(getPackageLevelResultsDirectory(classUnderTest.getPackageName(), operatorSet),
                classUnderTest.getClassName(),
                classUnderTest);
    }

    public static void serializeAnalysisReport(AnalysisReport analysisReport, String operatorSet) {
        serialize(getClassAnalysisDirectory(analysisReport.getFileName(), operatorSet),
                analysisReport.getFileName(),
                analysisReport);
    }

    public static void serializeClassAnalysisReport(ClassAnalysisReport classAnalysisReport, String operatorSet) {
        serialize(getClassAnalysisDirectory(classAnalysisReport.getPackageName(), operatorSet),
                classAnalysisReport.getClassName(),
                classAnalysisReport);
    }

    public static void serializeTestMethodReport(TestMethodReport testMethodReport, String operatorSet) {
        serialize(getTestClassLevelResultsDirectory(testMethodReport.getTestClassName(), operatorSet),
                testMethodReport.getTestMethodName(),
                testMethodReport);
    }

    private static void serialize(String dirName, String fileName, Object obj) {
        try {
            String serializeFileName = ensureEndsWith(fileName, SERIALIZE_FILE_EXTENSION);

            // ensure the results directory exists
            Files.createDirectories(Paths.get(dirName));

            // create the output stream to the file
            OutputStream fileOutputStream = Files.newOutputStream(Paths.get(dirName, serializeFileName));

            // serialize
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(fileOutputStream, obj);
        } catch (IOException e) {
            throw new PseudoSweepException(e);
        }
    }

    /*
     * DESERIALIZATION METHODS
     *
     */

    public static Set<ClassUnderTest> deserializeClassesUnderTest(String dataPath) {
        if (!dataPathExists(dataPath)) {
            throw new PseudoSweepException(dataPath + " directory does not exist.");
        }
        Set<String> filePaths = getSerializedFilePaths(dataPath);
        Set<ClassUnderTest> classesUnderTest = new HashSet<>();

        for (String filePath : filePaths) {
            File file = new File(filePath);
            String className = file.getName().replaceAll(SERIALIZE_FILE_EXTENSION, "");

            ClassUnderTest classUnderTest = new ClassUnderTest(filePath,
                    getClassPackageNameFromDirPath(filePath,
                            CLASSES_DIRECTORY_NAME), className);
            classUnderTest = (ClassUnderTest) deserialize(filePath, classUnderTest);

            classesUnderTest.add(classUnderTest);
        }
        return classesUnderTest;
    }

    public static Set<TestMethodReport> deserializeTestMethodReports(String dataPath) {
        if (!dataPathExists(dataPath)) {
            throw new PseudoSweepException(dataPath + " directory does not exist.");
        }
        Set<String> filePaths = getSerializedFilePaths(dataPath);
        Set<TestMethodReport> testMethodReports = new HashSet<>();

        for (String filePath : filePaths) {
            File file = new File(filePath);
            String testClassName = getClassPackageNameFromDirPath(filePath, RESULTS_DIRECTORY_NAME);
            String testMethodName = file.getName().replaceAll(SERIALIZE_FILE_EXTENSION, "");

            TestMethodReport testMethodReport = new TestMethodReport(testClassName, testMethodName);
            testMethodReport = (TestMethodReport) deserialize(filePath, testMethodReport);

            testMethodReports.add(testMethodReport);
        }
        return testMethodReports;

    }

    private static String getClassPackageNameFromDirPath(String filePath, String parentDir) {
        final String CLASS_SUBDIR = parentDir + File.separator;
        final int PACKAGE_NAME_START = filePath.lastIndexOf(CLASS_SUBDIR) + CLASS_SUBDIR.length();
        final int PACKAGE_NAME_END = filePath.lastIndexOf(File.separator);

        return filePath.substring(PACKAGE_NAME_START, PACKAGE_NAME_END).replace(File.separator, JavaConstants.PACKAGE_SEPARATOR);
    }

    private static Set<String> getSerializedFilePaths(String dataPath) {
        Set<String> fileList = new HashSet<>();
        if (dataPathExists(dataPath)) {
            try (Stream<Path> filePaths = Files.walk(Paths.get(dataPath))) {
                filePaths.forEach(x -> {
                    if (x.toString().endsWith(SERIALIZE_FILE_EXTENSION)) {
                        fileList.add(x.toString());
                    }
                });
            } catch (IOException e) {
                throw new PseudoSweepException(e);
            }

        }
        return fileList;
    }

    private static boolean dataPathExists(String dirName) {
        return Files.exists(Paths.get(dirName));
    }

    private static Object deserialize(String dirName, Object obj) {
        try {
            String deserializeFileName = ensureEndsWith(dirName, SERIALIZE_FILE_EXTENSION);

            if (dataPathExists(dirName)) {
                ObjectMapper objectMapper = new ObjectMapper();
                obj = objectMapper.readValue(new File(Paths.get(deserializeFileName).toString()), obj.getClass());
            }
        } catch (IOException e) {
            throw new PseudoSweepException(e);
        }

        return obj;
    }

}
