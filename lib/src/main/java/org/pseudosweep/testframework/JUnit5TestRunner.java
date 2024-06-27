package org.pseudosweep.testframework;


import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.pseudosweep.logging.LogManager;
import org.pseudosweep.logging.Logger;
import org.pseudosweep.PseudoSweepException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

public class JUnit5TestRunner extends TestRunner {
    private static final Logger logger = LogManager.getLogger(JUnit5TestRunner.class);

    TestOutcome executeTest(Method testMethod, Class<?> testClass) {

        if(testMethod.isAnnotationPresent(org.junit.jupiter.params.ParameterizedTest.class)) {
            logger.trace("Test skipped - Parameterized Test");
            return new TestOutcome(TestOutcome.Type.SKIPPED, 0);
        }

        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectMethod(testClass, testMethod.getName()))
                .build();
        Launcher launcher = LauncherFactory.create();
        launcher.discover(request);
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary es = listener.getSummary();
        long runtime = es.getTimeFinished() - es.getTimeStarted();

        String summary = generateSummary(testMethod, es);
        logger.trace("Test execution summary of " + summary);

        String error = checkInternalConsistency(es);
        if (error != null) {
            logger.warn(error + " for " + summary);
            return new TestOutcome(TestOutcome.Type.SKIPPED, runtime);
        }

        if (es.getTestsFailedCount() > 0) {
            logger.trace("Test failed");
            List<TestExecutionSummary.Failure> failures = es.getFailures();
            if (failures.size() != 1) {
                throw new PseudoSweepException("Failure reports cannot be > 1 for " + summary);
            }

            TestExecutionSummary.Failure firstFailure = failures.get(0);
            boolean isTestAssertionFailure = isAssertionFailure(firstFailure.getException());
            if (isTestAssertionFailure) {
                logger.trace("Reason: test assertion failure");
                return new TestOutcome(TestOutcome.Type.FAILED, runtime);
            } else {
                TestOutcome testOutcome = new TestOutcome(TestOutcome.Type.THREW_EXCEPTION, runtime);
                Throwable throwable = failures.get(0).getException();
                if (throwable instanceof NoClassDefFoundError) {
                    logger.warn("Check CLASSPATH – NoClassDefFoundError: " + throwable.getMessage());
                }

                testOutcome.setExceptionInfoFromException(throwable);
                logger.trace("Reason: exception thrown - " + testOutcome.getExceptionInfo());
                return testOutcome;
            }
        }

        boolean skipped = es.getTestsSkippedCount() == 1;
        if (skipped) {
            return new TestOutcome(TestOutcome.Type.SKIPPED, runtime);
        }

        boolean passed = es.getTestsSucceededCount() == 1;
        if (passed) {
            return new TestOutcome(TestOutcome.Type.PASSED, runtime);
        } else {
            throw new PseudoSweepException("Could not determine test outcome");
        }
    }

    String checkInternalConsistency(TestExecutionSummary es) {
        if (es.getTestsSucceededCount() > 1) {
            return "Tests succeeded cannot be > 1";
        }
        if (es.getTestsFailedCount() > 1) {
            return "Tests failed cannot be > 1 (Failed = " + es.getTestsFailedCount() + ")";
        }
        if (es.getTestsSkippedCount() > 1) {
            return "Tests skipped cannot be > 1";
        }
        if ((es.getTestsSucceededCount() == 0 && es.getTestsFailedCount() == 0) && es.getTestsSkippedCount() == 0) {
            return "Tests ignored must be equal to 1 when test succeeded/failed is 0";
        }
        if ((es.getTestsSucceededCount() == 1 || es.getTestsFailedCount() == 1) && es.getTestsSkippedCount() != 0) {
            return "Tests ignored must be equal to 0 when test succeeded/failed is 1";
        }

        return null;
    }

    String generateSummary(Method method, TestExecutionSummary es) {
        StringWriter out = new StringWriter();
        es.printTo(new PrintWriter(out));

        return method.getName() + " in class " +
                method.getDeclaringClass().getCanonicalName() +
                " with JUnit5. " + out;
    }

    boolean isRunnable(Method method) {
        return method.isAnnotationPresent(org.junit.jupiter.api.Test.class);
    }
}
