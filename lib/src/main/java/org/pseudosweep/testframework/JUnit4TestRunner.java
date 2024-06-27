package org.pseudosweep.testframework;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.pseudosweep.logging.LogManager;
import org.pseudosweep.logging.Logger;
import org.pseudosweep.PseudoSweepException;

import java.lang.reflect.Method;
import java.util.List;

public class JUnit4TestRunner extends TestRunner {

    private static final Logger logger = LogManager.getLogger(JUnit4TestRunner.class);

    TestOutcome executeTest(Method testMethod, Class<?> testClass) {

        JUnitCore junit = new JUnitCore();
        Result result = junit.run(Request.method(testClass, testMethod.getName()));

        long runtime = result.getRunTime();

        String summary = generateSummary(testMethod, result);
        logger.trace("Test execution summary of " + summary);

        String error = checkInternalConsistency(result);
        if (error != null) {
            logger.warn(error + " for " + summary);
            return new TestOutcome(TestOutcome.Type.SKIPPED, runtime);
        }
        
        if (result.getFailureCount() > 0) {
            logger.trace("Test failed");
            List<Failure> failures = result.getFailures();
            if (failures.size() != 1) {
                throw new PseudoSweepException("Failure reports cannot be > 1 for " + summary);
            }

            Failure firstFailure = failures.get(0);
            boolean isTestAssertionFailure = isAssertionFailure(firstFailure.getException());
            if (isTestAssertionFailure) {
                logger.trace("Reason: test assertion failure: " + firstFailure.getException());
                return new TestOutcome(TestOutcome.Type.FAILED, runtime);
            } else {
                TestOutcome testOutcome = new TestOutcome(TestOutcome.Type.THREW_EXCEPTION, runtime);
                Throwable throwable = firstFailure.getException();
                if (throwable instanceof NoClassDefFoundError) {
                    logger.warn("Check CLASSPATH – NoClassDefFoundError: " + throwable.getMessage());
                }

                testOutcome.setExceptionInfoFromException(throwable);
                logger.trace("Reason: exception thrown: " + testOutcome.getExceptionInfo());
                return testOutcome;
            }
        }

        boolean skipped = result.getIgnoreCount() == 1;
        if (skipped) {
            return new TestOutcome(TestOutcome.Type.SKIPPED, runtime);
        }

        boolean passed = result.getFailureCount() == 0;
        if (passed) {
            return new TestOutcome(TestOutcome.Type.PASSED, runtime);

        } else {
            throw new PseudoSweepException("Could not determine test outcome");
        }
    }

    String checkInternalConsistency(Result result) {
        if (result.getRunCount() > 1) {
            return "Tests run cannot be > 1";
        }
        if (result.getFailureCount() > 1) {
            return "Tests failed cannot be > 1";
        }
        if (result.getIgnoreCount() > 1) {
            return "Tests ignored cannot be > 1";
        }
        if (result.getFailureCount() == 0 && (result.getRunCount() + result.getIgnoreCount() != 1)) {
            return "Tests run or ignored must be equal to 1 when test failures is 0";
        }
        if (result.getFailureCount() == 1 && (result.getRunCount() != 1 || result.getIgnoreCount() != 0)) {
            return "Failed tests must be equal to tests run";
        }

        return null;
    }

    String generateSummary(Method method, Result result) {
        return method.getName() + " in class " + method.getDeclaringClass().getCanonicalName() +
                " with JUnit4. Failures: " + result.getFailureCount() + ". Ignored: " +
                result.getIgnoreCount() + ". Tests run: " + result.getRunCount() + ". Time: " +
                result.getRunTime() + "ms.";
    }

    boolean isRunnable(Method method) {
        return method.isAnnotationPresent(org.junit.Test.class);
    }
}
