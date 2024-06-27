package org.pseudosweep;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.pseudosweep.program.CoverageElement;
import org.pseudosweep.program.Decision;
import org.pseudosweep.program.Stmt;
import org.pseudosweep.testframework.TestOutcome;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class TestMethodReportTest {

    @Test
    public void serializationAndDeserialization() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TestMethodReport testMethodReport = new TestMethodReport("com.test.Test", "test");

        List<TestOutcome> testOutcomes = new ArrayList<>();
        TestOutcome to1 = new TestOutcome(TestOutcome.Type.PASSED, 200);
        TestOutcome to2 = new TestOutcome(TestOutcome.Type.PASSED, 200);
        TestOutcome to3 = new TestOutcome(TestOutcome.Type.PASSED, 200);
        testOutcomes.add(to1);
        testOutcomes.add(to2);
        testOutcomes.add(to3);
        testMethodReport.setTestOutcomes(testOutcomes);

        Stmt stmt1 = new Stmt(Stmt.Type.IF, 0, "com.test.Test", true);
        Stmt stmt2 = new Stmt(Stmt.Type.WHILE, 1, "com.test.Test", true);

        Set<CoverageElement> covered = new HashSet<>();
        covered.add(stmt1);
        covered.add(stmt2);
        testMethodReport.setCovered(covered);

        List<TestOutcome> stmt1TestOutcomes = new ArrayList<>();
        TestOutcome bto1 = new TestOutcome(TestOutcome.Type.PASSED, 200);
        TestOutcome bto2 = new TestOutcome(TestOutcome.Type.FAILED, 300);
        TestOutcome bto3 = new TestOutcome(TestOutcome.Type.THREW_EXCEPTION, 300);
        bto3.setExceptionInfo("java.lang.NullPointerException");
        stmt1TestOutcomes.add(bto1);
        stmt1TestOutcomes.add(bto2);
        stmt1TestOutcomes.add(bto3);
        testMethodReport.setTargetTestOutcomesForCoverageElement(stmt1, stmt1TestOutcomes);

        List<TestOutcome> stmt2TestOutcomes = new ArrayList<>();
        TestOutcome bto4 = new TestOutcome(TestOutcome.Type.TIMED_OUT, 1200);
        TestOutcome bto5 = new TestOutcome(TestOutcome.Type.FAILED, 300);
        bto3.setExceptionInfo("java.lang.NullPointerException");
        stmt2TestOutcomes.add(bto4);
        stmt2TestOutcomes.add(bto5);
        testMethodReport.setTargetTestOutcomesForCoverageElement(stmt2, stmt2TestOutcomes);

        // serialize
        String json = objectMapper.writeValueAsString(testMethodReport);

        // deserialize
        TestMethodReport in = objectMapper.readValue(json, TestMethodReport.class);
        assertThat(in.getTestClassName(), equalTo("com.test.Test"));
        assertThat(in.getTestMethodName(), equalTo("test"));
        assertThat(in.getCovered(), equalTo(covered));
        assertThat(in.getCoverageElementTestOutcomes().get(stmt1), equalTo(stmt1TestOutcomes));
        assertThat(in.getCoverageElementTestOutcomes().get(stmt2), equalTo(stmt2TestOutcomes));
    }

    @Test
    public void computeTimeoutFromPassingInitialTests_Minimum() {
        TestMethodReport testMethodReport = new TestMethodReport("com.Test", "test");
        List<TestOutcome> testOutcomes = new ArrayList<>();
        testOutcomes.add(new TestOutcome(TestOutcome.Type.PASSED, 1));
        testOutcomes.add(new TestOutcome(TestOutcome.Type.PASSED, 1));
        testOutcomes.add(new TestOutcome(TestOutcome.Type.PASSED, 1));
        testMethodReport.setTestOutcomes(testOutcomes);
        assertThat(testMethodReport.computeTimeoutFromPassingInitialTests(), equalTo(
                TestMethodReport.MIN_MAX * TestMethodReport.TIMEOUT_MULTIPLIER));
    }

    @Test
    public void computeTimeoutFromPassingInitialTests() {
        TestMethodReport testMethodReport = new TestMethodReport("com.Test", "test");
        List<TestOutcome> testOutcomes = new ArrayList<>();
        testOutcomes.add(new TestOutcome(TestOutcome.Type.PASSED, 500));
        testOutcomes.add(new TestOutcome(TestOutcome.Type.PASSED, 1500));
        testOutcomes.add(new TestOutcome(TestOutcome.Type.PASSED, 1750));
        testMethodReport.setTestOutcomes(testOutcomes);
        assertThat(testMethodReport.computeTimeoutFromPassingInitialTests(), equalTo(
                1750L * TestMethodReport.TIMEOUT_MULTIPLIER));
    }

    @Test
    public void getEffectualCoverageTargets() {
        Stmt b1 = new Stmt(Stmt.Type.IF, 0, "Test", true);
        Stmt b2 = new Stmt(Stmt.Type.WHILE, 2, "Test", true);
        Decision d = new Decision(true, Decision.Type.IF, 1, "Test");
        Set<CoverageElement> covered = new HashSet<>();
        covered.add(b1);
        covered.add(b2);
        covered.add(d);

        TestMethodReport testMethodReport = new TestMethodReport("com.Test", "test");
        testMethodReport.setCovered(covered);

        Set<CoverageElement> targets = testMethodReport.getEffectualCoverageTargets();

        assertThat(targets.size(), equalTo(3));
        assertThat(targets.contains(b1), equalTo(true));
        assertThat(targets.contains(b2), equalTo(true));
        assertThat(targets.contains(d), equalTo(true));
    }
}
