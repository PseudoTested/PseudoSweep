package org.pseudosweep.testframework;

import org.pseudosweep.testresources.examples.JUnit4TestClass;
import org.junit.jupiter.api.Test;
import org.pseudosweep.testresources.examples.JUnit5TestClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringStartsWith.startsWith;

public class JUnit4TestRunnerTest {
    final Class<?> testClass = JUnit4TestRunnerTest.class;

    @Test
    public void isATest() throws NoSuchMethodException {
        assertThat(new JUnit4TestRunner().isRunnable(JUnit4TestClass.class.getMethod("junit4TestExample")), is(true));
    }

    @Test
    public void notAJUnit4Test() throws NoSuchMethodException {
        assertThat(new JUnit4TestRunner().isRunnable(JUnit5TestClass.class.getMethod("junit5TestExample")), is(false));
    }

    @Test
    public void notATest() throws NoSuchMethodException {
        assertThat(new JUnit4TestRunner().isRunnable(JUnit4TestClass.class.getMethod("notATest")), is(false));
    }

    @Test
    public void passingJTestRecordedAsPassed() throws NoSuchMethodException {
        TestOutcome outcome = TestRunners.executeTest(JUnit4TestClass.class.getMethod("passes"), testClass);
        assertThat(outcome.getType(), equalTo(TestOutcome.Type.PASSED));
    }

    @Test
    public void failingTestRecordedAsFailed() throws NoSuchMethodException {
        TestOutcome outcome = TestRunners.executeTest(JUnit4TestClass.class.getMethod("fails"), testClass);
        assertThat(outcome.getType(), equalTo(TestOutcome.Type.FAILED));
    }

    @Test
    public void skippedTestRecordedAsSkipped() throws NoSuchMethodException {
        TestOutcome outcome = TestRunners.executeTest(JUnit4TestClass.class.getMethod("skipped"), testClass);
        assertThat(outcome.getType(), equalTo(TestOutcome.Type.SKIPPED));
    }

    @Test
    public void throwsExceptionTestRecordedAsThrowsException() throws NoSuchMethodException {
        TestOutcome outcome = TestRunners.executeTest(JUnit4TestClass.class.getMethod("throwsException"), testClass);
        assertThat(outcome.getType(), equalTo(TestOutcome.Type.THREW_EXCEPTION));
        assertThat(outcome.getExceptionInfo(), startsWith("java.lang.RuntimeException"));
    }

    @Test
    public void timesOutTestRecordedAsTimedOut() throws NoSuchMethodException {
        long timeout = 100;
        TestOutcome outcome = TestRunners.executeTest(JUnit4TestClass.class.getMethod("timesOut"),testClass, timeout);
        assertThat(outcome.getType(), equalTo(TestOutcome.Type.TIMED_OUT));
        assertThat(outcome.getRunTime(), equalTo(timeout));
    }

}
