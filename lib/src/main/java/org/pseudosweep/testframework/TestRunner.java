package org.pseudosweep.testframework;

import org.pseudosweep.logging.LogManager;
import org.pseudosweep.logging.Logger;
import org.pseudosweep.util.TimeLimitedCodeBlock;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.pseudosweep.util.StringUtils.encloseQuotes;

public abstract class TestRunner {

    private static final Set<Class<?>> assertionFailureClasses =
            Set.of(java.lang.AssertionError.class, org.opentest4j.AssertionFailedError.class);

    private static final Logger logger = LogManager.getLogger(TestRunner.class);

    abstract boolean isRunnable(Method method);

    abstract TestOutcome executeTest(Method testMethod, Class<?> testClass);

    public TestOutcome executeTest(Method testMethod, Class<?> testClass, long testTimeout) {
        try {
            return TimeLimitedCodeBlock.runWithTimeout(() -> {
                logger.trace("Executing test with timeout " + testTimeout);
                return executeTest(testMethod, testClass);
            }, testTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            logger.trace("Execution timed out after " + testTimeout + "s for " + encloseQuotes(testMethod));
            return new TestOutcome(TestOutcome.Type.TIMED_OUT, testTimeout);
        }
    }

    static boolean isAssertionFailure(Throwable t) {
        return assertionFailureClasses.contains(t.getClass());
    }
}
