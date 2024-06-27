package org.pseudosweep.analysis;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pseudosweep.TestMethodReport;
import org.pseudosweep.analysis.report.ClassAnalysisReport;
import org.pseudosweep.analysis.report.ProjectOverviewReport;
import org.pseudosweep.program.*;
import org.pseudosweep.testframework.TestOutcome;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.pseudosweep.analysis.Analysis.getElementsOfType;
import static org.pseudosweep.analysis.sdl.TypeSDL.*;
import static org.pseudosweep.analysis.xmt.TypeXMT.METHOD;


class AnalysisTest {

    final String OPERATOR_SET = "sdl";
    Set<TestMethodReport> testMethodReportSet;
    Set<ClassUnderTest> classUnderTestSet = getClassUnderTestSet();
    List<String> paramsString;
    List<Type> paramsType;

    @BeforeEach
    void before() {
        paramsString = Arrays.asList(
                "BREAK",
                "CONTINUE",
                "DECISION",
                "DO",
                "EXPRESSION",
                "FOR",
                "FOR_EACH",
                "IF",
                "RETURN",
                "SWITCH",
                "TRY",
                "WHILE");
        paramsType = new ArrayList<>();
        for (String p : paramsString) {
            paramsType.add(valueOf(p));
        }
        testMethodReportSet = getTestMethodReportSet();
        classUnderTestSet = getClassUnderTestSet();
    }

    @AfterEach
    void after() {
        paramsString = null;
        paramsType = null;
        testMethodReportSet = null;
        classUnderTestSet = null;
    }

    @Test
    void test_collateCoverageElements() {

        Stmt stmt1 = new Stmt(Stmt.Type.IF, 0, "ClassUnderTest", true);
        Stmt stmt2 = new Stmt(Stmt.Type.FOR, 1, "ClassUnderTest", true);
        Stmt stmt3 = new Stmt(Stmt.Type.FOR, 2, "ClassUnderTest", true);
        Stmt stmt4 = new Stmt(Stmt.Type.IF, 0, "ClassUnderTestTwo", true);
        Stmt stmt5 = new Stmt(Stmt.Type.FOR, 1, "ClassUnderTestTwo", true);
        Stmt stmt6 = new Stmt(Stmt.Type.FOR, 2, "ClassUnderTestTwo", true);

        // Expected covered elements

        Set<CoverageElement> allCoveredExpected = new HashSet<>();
        allCoveredExpected.add(stmt1);
        allCoveredExpected.add(stmt2);
        allCoveredExpected.add(stmt3);
        allCoveredExpected.add(stmt4);
        allCoveredExpected.add(stmt5);
        allCoveredExpected.add(stmt6);

        // Expected effectually covered elements
        Set<CoverageElement> allEffectualCoveredExpected = new HashSet<>();
        allEffectualCoveredExpected.add(stmt2);
        allEffectualCoveredExpected.add(stmt5);


        // Expected Ineffectual Elements
        Set<CoverageElement> allIneffectualCoveredExpected = new HashSet<>();
        allIneffectualCoveredExpected.add(stmt1);
        allIneffectualCoveredExpected.add(stmt3);
        allIneffectualCoveredExpected.add(stmt4);
        allIneffectualCoveredExpected.add(stmt6);

        Analysis analysis = new Analysis(testMethodReportSet, paramsString, OPERATOR_SET);
        // run
        analysis.collateCoverageElements(testMethodReportSet);

        // check
        assertThat(analysis.allCovered, Matchers.containsInAnyOrder(stmt1, stmt2, stmt3, stmt4, stmt5, stmt6));
        assertThat(analysis.allEffectualCovered, Matchers.containsInAnyOrder(stmt2, stmt5));
        assertThat(analysis.allIneffectualCovered, Matchers.containsInAnyOrder(stmt1, stmt3, stmt4, stmt6));
//
        // check values add up
        int expected = allCoveredExpected.size() - allEffectualCoveredExpected.size() - allIneffectualCoveredExpected.size();
        assertEquals(expected, 0);
        assertEquals(expected, analysis.allCovered.size() - analysis.allEffectualCovered.size() - analysis.allIneffectualCovered.size());
    }


    @Test
    void test_addMetrics_ClassAnalysisReport() {
        // Build expected class analysis report
        Stmt b1 = new Stmt(Stmt.Type.IF, 0, "ClassUnderTest", true);
        Set<CoverageElement> expectedCoverageGap = new HashSet<>();
        expectedCoverageGap.add(b1);

        // Run method for result
        Analysis analysis = new Analysis(testMethodReportSet, paramsString, OPERATOR_SET);
        ClassAnalysisReport result = analysis.addMetrics(createClassUnderTest("ClassUnderTest.java", "com.test", "ClassUnderTest"), new ClassAnalysisReport("ClassUnderTest.java", "com.test", "ClassUnderTest", paramsType));

        // Check output
        assertEquals(2, result.getTypeMetricsHashMap().get(IF).getElementCount());
        assertEquals(1, result.getTypeMetricsHashMap().get(IF).getCoveredCount());
        assertIterableEquals(expectedCoverageGap, result.getTypeMetricsHashMap().get(IF).getCoverageGap());
    }

    @Test
    void test_addMetrics_ProjectOverviewReport() {
        // Build expected class analysis report
        Stmt s1 = new Stmt(Stmt.Type.IF, 0, "ClassUnderTest", true);
        Stmt s2 = new Stmt(Stmt.Type.IF, 0, "ClassUnderTestTwo", true);


        Set<CoverageElement> expectedCoverageGap = new HashSet<>();
        expectedCoverageGap.add(s1);


        // Run method for result
        Analysis analysis = new Analysis(testMethodReportSet, paramsString, OPERATOR_SET);


        ProjectOverviewReport result = analysis.addMetrics(createClassUnderTest("ClassUnderTest.java", "com.test", "ClassUnderTest"), new ProjectOverviewReport(paramsType));

        // Check output
        assertEquals(2, result.getTypeMetricsHashMap().get(IF).getElementCount());
        assertEquals(1, result.getTypeMetricsHashMap().get(IF).getCoveredCount());
        assertIterableEquals(expectedCoverageGap, result.getTypeMetricsHashMap().get(IF).getCoverageGap());

        expectedCoverageGap.add(s2);
        ClassUnderTest classUnderTest = createClassUnderTest("ClassUnderTestTwo.java", "com.test", "ClassUnderTestTwo");

        result = analysis.addMetrics(classUnderTest, result);

        // Check output
        assertEquals(4, result.getTypeMetricsHashMap().get(IF).getElementCount());
        assertEquals(2, result.getTypeMetricsHashMap().get(IF).getCoveredCount());
        assertIterableEquals(expectedCoverageGap, result.getTypeMetricsHashMap().get(IF).getCoverageGap());

    }

    @Test
    void test_getElementsOfType_givenTypeMETHOD() {
        Block b1 = new Block(Block.Type.METHOD, 6, "ClassUnderTest", true);
        Block b2 = new Block(Block.Type.METHOD, 6, "ClassUnderTest", false);
        Set<CoverageElement> expected = new HashSet<>();
        expected.add(b1);
        expected.add(b2);
        Set<CoverageElement> result = getElementsOfType(createClassUnderTest("ClassUnderTest.java", "com.test", "ClassUnderTest"), METHOD);
        assertIterableEquals(expected, result);
    }

    @Test
    void test_getElementsOfType_givenTypeFOR_EACH() {
        Stmt b1 = new Stmt(Stmt.Type.FOR_EACH, 6, "ClassUnderTest", true);
        Set<CoverageElement> expected = new HashSet<>();
        expected.add(b1);
        Set<CoverageElement> result = getElementsOfType(createClassUnderTest("ClassUnderTest.java", "com.test", "ClassUnderTest"), FOR_EACH);
        assertIterableEquals(expected, result);
    }

    @Test
    void test_getElementsOfType_givenTypeFOR() {
        Stmt b3 = new Stmt(Stmt.Type.FOR, 5, "ClassUnderTest", true);
        Set<CoverageElement> expected = new HashSet<>();
        expected.add(b3);

        Set<CoverageElement> result = getElementsOfType(createClassUnderTest("ClassUnderTest.java", "com.test", "ClassUnderTest"), FOR);
        assertIterableEquals(expected, result);
    }


//   ----- Helper methods ------

    private Set<TestMethodReport> getTestMethodReportSet() {
        Set<TestMethodReport> testMethodReports = new HashSet<>();

        TestMethodReport testMethodReport = createTestMethodReport("ClassUnderTest");
        TestMethodReport testMethodReport2 = createTestMethodReport("ClassUnderTestTwo");

        testMethodReports.add(testMethodReport);
        testMethodReports.add(testMethodReport2);

        return testMethodReports;
    }

    private Set<ClassUnderTest> getClassUnderTestSet() {
        Set<ClassUnderTest> classUnderTests = new HashSet<>();

        ClassUnderTest classUnderTest = createClassUnderTest("ClassUnderTest.java", "com.test", "ClassUnderTest");


        classUnderTests.add(classUnderTest);


        return classUnderTests;
    }

    private TestMethodReport createTestMethodReport(String containingClass) {
        TestMethodReport testMethodReport = new TestMethodReport("com.Test", "test");

        List<TestOutcome> testOutcomes = new ArrayList<>();
        TestOutcome to1 = new TestOutcome(TestOutcome.Type.PASSED, 200);
        TestOutcome to2 = new TestOutcome(TestOutcome.Type.PASSED, 200);
        TestOutcome to3 = new TestOutcome(TestOutcome.Type.PASSED, 200);
        testOutcomes.add(to1);
        testOutcomes.add(to2);
        testOutcomes.add(to3);
        testMethodReport.setTestOutcomes(testOutcomes);

        Stmt stmt1 = new Stmt(Stmt.Type.IF, 0, containingClass, true);
        Stmt stmt2 = new Stmt(Stmt.Type.FOR, 1, containingClass, true);
        Stmt stmt3 = new Stmt(Stmt.Type.FOR, 2, containingClass, true);

        Set<CoverageElement> covered = new HashSet<>();
        covered.add(stmt1);
        covered.add(stmt2);
        covered.add(stmt3);
        testMethodReport.setCovered(covered);

        // Ineffectually Covered
        List<TestOutcome> stmt1TestOutcomes = new ArrayList<>();
        TestOutcome bto1 = new TestOutcome(TestOutcome.Type.PASSED, 200);
        TestOutcome bto2 = new TestOutcome(TestOutcome.Type.PASSED, 300);
        TestOutcome bto3 = new TestOutcome(TestOutcome.Type.PASSED, 300);
        bto3.setExceptionInfo("java.lang.NullPointerException");
        stmt1TestOutcomes.add(bto1);
        stmt1TestOutcomes.add(bto2);
        stmt1TestOutcomes.add(bto3);
        testMethodReport.setTargetTestOutcomesForCoverageElement(stmt1, stmt1TestOutcomes);


        // Effectually Covered
        List<TestOutcome> stmt2TestOutcomes = new ArrayList<>();
        TestOutcome bto4 = new TestOutcome(TestOutcome.Type.TIMED_OUT, 1200);
        TestOutcome bto5 = new TestOutcome(TestOutcome.Type.FAILED, 300);
        stmt2TestOutcomes.add(bto4);
        stmt2TestOutcomes.add(bto5);
        testMethodReport.setTargetTestOutcomesForCoverageElement(stmt2, stmt2TestOutcomes);

        return testMethodReport;
    }

    private ClassUnderTest createClassUnderTest(String fileName, String packageName, String className) {
        ClassUnderTest classUnderTest = new ClassUnderTest(fileName, packageName, className);

        Stmt s0 = new Stmt(Stmt.Type.WHILE, 1, className, true);
        Stmt s1 = new Stmt(Stmt.Type.IF, 0, className, true);
        Stmt s2 = new Stmt(Stmt.Type.IF, 3, className, true);
        Stmt s3 = new Stmt(Stmt.Type.FOR, 5, className, true);
        Stmt s4 = new Stmt(Stmt.Type.FOR_EACH, 6, className, true);
        Decision d1 = new Decision(true, Decision.Type.IF, 1, className);
        Decision d2 = new Decision(true, Decision.Type.LOOP, 4, className);
        Block b1 = new Block(Block.Type.METHOD, 6, "ClassUnderTest", true);
        Block b2 = new Block(Block.Type.METHOD, 6, "ClassUnderTest", false);
        classUnderTest.addCoverageElement(s0);
        classUnderTest.addCoverageElement(s1);
        classUnderTest.addCoverageElement(s2);
        classUnderTest.addCoverageElement(s3);
        classUnderTest.addCoverageElement(s4);
        classUnderTest.addCoverageElement(d1);
        classUnderTest.addCoverageElement(d2);
        classUnderTest.addCoverageElement(b1);
        classUnderTest.addCoverageElement(b2);

        SourceFilePosition sourceFilePosition = new SourceFilePosition(1, 1, 2, 2);
        classUnderTest.setPosition(b1, sourceFilePosition);

        return classUnderTest;
    }


}
