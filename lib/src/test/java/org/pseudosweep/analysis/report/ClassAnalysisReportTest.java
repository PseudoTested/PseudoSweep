package org.pseudosweep.analysis.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pseudosweep.analysis.Metrics;
import org.pseudosweep.analysis.Type;
import org.pseudosweep.program.CoverageElement;
import org.pseudosweep.program.Stmt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.pseudosweep.analysis.sdl.TypeSDL.*;

class ClassAnalysisReportTest {


    List<Type> params;


    @BeforeEach
    void setUp() {
        params = Arrays.asList(
                BREAK,
                CONTINUE,
                DECISION,
                DO,
                EXPRESSION,
                FOR,
                FOR_EACH,
                IF,
                RETURN,
                SWITCH,
                SWITCH_ENTRY_ASSIGNMENT,
                THROW,
                TRY,
                VARIABLE_DECLARATION,
                WHILE);
    }

    @Test
    void getTotalElementCount() {
        ClassAnalysisReport analysisReport = getClassAnalysisReport();

        int expected = 15 * 5;
        int result = analysisReport.getTotalElementCount();
        assertEquals(expected, result);
    }


    @Test
    void getTotalCoveredCount() {
        ClassAnalysisReport analysisReport = getClassAnalysisReport();

        int expected = 15 * 4;
        int result = analysisReport.getTotalCoveredCount();
        assertEquals(expected, result);
    }

    @Test
    void getTotalEffectualCount() {
        ClassAnalysisReport analysisReport = getClassAnalysisReport();

        int expected = 15 * 3;
        int result = analysisReport.getTotalEffectualCount();
        assertEquals(expected, result);
    }

    @Test
    void getTotalCoverageGap() {
        ClassAnalysisReport analysisReport = getClassAnalysisReport();

        int expected = 15 * 2;
        int result = analysisReport.getTotalCoverageGap();
        assertEquals(expected, result);
    }


    //    HELPER METHODS

    private ClassAnalysisReport getClassAnalysisReport() {
        ClassAnalysisReport analysisReport = new ClassAnalysisReport("ClassUnderTest.java", "com.test", "ClassUnderTest", params);
        params.forEach(metric -> {
            Metrics metrics = new Metrics();
            metrics.updateMetrics(5, 4, 3, getIneffectualSet());

            analysisReport.setTypeMetricsHashMap(metric, metrics);

        });
        return analysisReport;
    }

    Set<CoverageElement> getIneffectualSet() {
        Set<CoverageElement> ineffectual = new HashSet<>();

        Stmt s0 = new Stmt(Stmt.Type.IF, 0, "ClassUnderTest", true);
        Stmt s1 = new Stmt(Stmt.Type.EXPRESSION, 2, "ClassUnderTest", true);

        ineffectual.add(s0);
        ineffectual.add(s1);

        return ineffectual;
    }


}
