package org.pseudosweep.testframework;

import org.pseudosweep.PseudoSweepException;

import java.lang.reflect.Method;
import java.util.Set;

import static org.pseudosweep.util.StringUtils.encloseQuotes;

public class TestRunners {

    static Set<TestRunner> testRunners = Set.of(
            new JUnit4TestRunner(),
            new JUnit5TestRunner());

    static TestRunner getTestRunner(Method testMethod) {
        for (TestRunner testRunner: testRunners) {
            if (testRunner.isRunnable(testMethod)) {
                return testRunner;
            }
        }
        return null;
    }

    public static TestOutcome executeTest(Method testMethod,Class<?> testClass) {
        return executeTest(testMethod, testClass, Long.MAX_VALUE);
    }

    public static TestOutcome executeTest(Method testMethod, Class<?> testClass, long timeout) {
        TestRunner testRunner = getTestRunner(testMethod);
        if (testRunner == null) {
            throw new PseudoSweepException("No available TestRunner for " + encloseQuotes(testMethod));
        }
        return testRunner.executeTest(testMethod, testClass, timeout);
    }

    public static boolean isExecutableTestMethod(Method method) {
        return getTestRunner(method) != null;
    }
}
