package org.pseudosweep.analysis;

import org.pseudosweep.program.Stmt;
import org.pseudosweep.program.CoverageElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsTest {

    Metrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new Metrics();
    }

    @Test
    void updateMetricsByType() {

        metrics.updateMetrics(5, 4, 2, getIneffectualSet());

        assertEquals(5, metrics.getElementCount());
        assertEquals(4, metrics.getCoveredCount());
        assertEquals(2, metrics.getEffectualCount());
        assertEquals(2, metrics.getCoverageGapSize());

        Set<CoverageElement> ineffectual = new HashSet<>();
        Stmt b2 = new Stmt(Stmt.Type.WHILE, 4, "ClassUnderTest", true);
        Stmt b3 = new Stmt(Stmt.Type.IF, 5, "ClassUnderTest", true);
        ineffectual.add(b2);
        ineffectual.add(b3);

        metrics.updateMetrics(4, 4, 2, ineffectual);
        assertEquals(9, metrics.getElementCount());
        assertEquals(8, metrics.getCoveredCount());
        assertEquals(4, metrics.getEffectualCount());
        assertEquals(4, metrics.getCoverageGap().size());

    }

    @Test
    void getElementCount() {
        metrics.updateMetrics(5, 4, 3, getIneffectualSet());

        int expected = 5;

        assertEquals(expected, metrics.getElementCount());
    }

    @Test
    void getCoveredCount() {
        metrics.updateMetrics(5, 4, 3, getIneffectualSet());

        int expected = 4;

        assertEquals(expected, metrics.getCoveredCount());
    }

    @Test
    void getCoveredPercentage() {
        metrics.updateMetrics(5, 4, 3, getIneffectualSet());

        double expected = 80;

        assertEquals(expected, metrics.getCoveredPercentage());
    }

    @Test
    void getEffectualCount() {
        metrics.updateMetrics(5, 4, 3, getIneffectualSet());

        int expected = 3;

        assertEquals(expected, metrics.getEffectualCount());
    }

    @Test
    void getEffectualPercentage() {
        metrics.updateMetrics(5, 4, 3, getIneffectualSet());

        double expected = 60;

        assertEquals(expected, metrics.getEffectualPercentage());
    }

    @Test
    void getCoverageGapSize() {
        metrics.updateMetrics(5, 4, 3, getIneffectualSet());

        double expected = 2;

        assertEquals(expected, metrics.getCoverageGapSize());
    }

    @Test
    void getCoverageGapPercentage() {
        metrics.updateMetrics(5, 4, 3, getIneffectualSet());

        double expected = 40;

        assertEquals(expected, metrics.getCoverageGapPercentage());
    }

    @Test
    void getCoverageGap() {
        metrics.updateMetrics(5, 4, 3, getIneffectualSet());

        Set<CoverageElement> expected = getIneffectualSet();

        assertEquals(expected.size(), metrics.getCoverageGap().size());

    }

//    HELPER METHODS

    Set<CoverageElement> getIneffectualSet() {
        Set<CoverageElement> ineffectual = new HashSet<>();

        Stmt b0 = new Stmt(Stmt.Type.WHILE, 0, "ClassUnderTest", true);
        Stmt b1 = new Stmt(Stmt.Type.IF, 2, "ClassUnderTest", true);

        ineffectual.add(b0);
        ineffectual.add(b1);

        return ineffectual;
    }
}
