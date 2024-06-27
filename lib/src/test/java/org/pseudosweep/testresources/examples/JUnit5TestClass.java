package org.pseudosweep.testresources.examples;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class JUnit5TestClass {

    @Test
    public void junit5TestExample() {
        // used for testing the TestRunners – doesn't do anything ....
    }

    public void notATest() {

    }

    @Test
    public void passes() {
        assertThat(true, equalTo(true));
    }

    @Test
    public void fails() {
        assertThat(false, equalTo(true));
    }

    @Test @Disabled
    public void skipped() {
        assertThat("pigs", equalTo("flying creatures"));
    }

    @Test
    public void throwsException() {
        throw new RuntimeException("I've got a real problem");
    }

    @Test
    public void timesOut() {
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
        }
    }
}
