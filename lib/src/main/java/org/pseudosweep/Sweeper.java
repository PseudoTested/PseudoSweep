package org.pseudosweep;

import org.pseudosweep.logging.LogManager;
import org.pseudosweep.logging.Logger;
import org.pseudosweep.program.CoverageElement;
import org.pseudosweep.testframework.TestRunners;
import org.pseudosweep.util.Stopwatch;

import java.lang.reflect.Method;

public class Sweeper {

    private static final Logger logger = LogManager.getLogger(Sweeper.class);

    private final TestExecutionManager testExecutionManager;
    private final long testTimeout;

    public Sweeper(int testRepeats, long testTimeout) {
        testExecutionManager = TestExecutionManager.instance();
        testExecutionManager.setTestRepeats(testRepeats);
        this.testTimeout = testTimeout;
    }

    public TestMethodReport sweep(Method testMethod, Class<?> testClass) {
        logger.trace("STARTING METHOD: " + testMethod.getName() + " | " + testClass);

        if (!TestRunners.isExecutableTestMethod(testMethod)) {
            logger.trace("Ignoring as not an executable test method");
            return null;
        }

        TestMethodReport report = new TestMethodReport(testMethod, testClass.getCanonicalName());
        Stopwatch stopwatch = Stopwatch.start();

        testExecutionManager.setTestTimeout(testTimeout);

        logger.trace("Finding execution coverage...");
        testExecutionManager.findCoverage(report, testMethod, testClass, null);

        if (report.allInitialTestsPassed()) {
            logger.trace("Finding effectual coverage...");

            long runningTime = report.computeTimeoutFromPassingInitialTests();
            testExecutionManager.setTestTimeout(runningTime);
            logger.trace("Setting timeout for effectual coverage tests at " + runningTime + "ms.");

            if (report.getEffectualCoverageTargets().isEmpty()) {
                logger.warn("No target elements, check code is instrumented and compiled class is on classpath");
            }
            for (CoverageElement coverageElement : report.getEffectualCoverageTargets()) {
                logger.trace("Setting " + coverageElement + " as the effectual coverage target...");
                testExecutionManager.findCoverage(report, testMethod, testClass, coverageElement);
            }

            report.setTimeTaken(stopwatch.duration().toMillis());
        } else {
            logger.trace("Not all tests passed, so not computing effectual coverage.");
        }

        return report;
    }
}

