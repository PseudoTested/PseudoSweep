package org.pseudosweep.analysis;


import org.apache.commons.math3.util.Precision;
import org.pseudosweep.program.CoverageElement;

import java.util.HashSet;
import java.util.Set;
public class Metrics {

    private int elementCount;
    private int coveredCount;
    private int effectualCount;
    private Set<CoverageElement> coverageGap;


    public Metrics() {
        this.elementCount = 0;
        this.coveredCount = 0;
        this.effectualCount = 0;
        this.coverageGap = new HashSet<>();
    }

    public void updateMetrics(int elementCount, int coveredCount, int effectualCount, Set<CoverageElement> ineffectual) {
        this.elementCount += elementCount;
        this.coveredCount += coveredCount;
        this.effectualCount += effectualCount;
        this.coverageGap.addAll(ineffectual);
    }


    public int getElementCount() {
        return this.elementCount;
    }

    public int getCoveredCount() {
        return this.coveredCount;
    }

    public double getCoveredPercentage() {
        return (this.coveredCount == 0) ? 0 : Precision.round(((double) this.coveredCount / (double) this.elementCount) * 100,2);
    }

    public int getEffectualCount() {
        return this.effectualCount;
    }

    public double getEffectualPercentage() {
        return (this.effectualCount == 0) ? 0 : Precision.round(((double) this.effectualCount / (double) this.elementCount) * 100,2);
    }

    public int getCoverageGapSize() {
        return this.coverageGap.size();
    }

    public double getCoverageGapPercentage() {
        return (getCoverageGapSize() == 0) ? 0 : Precision.round((double) getCoverageGapSize() / (double) this.elementCount * 100,2);
    }

    public Set<CoverageElement> getCoverageGap() {
            return this.coverageGap;
        }

}
