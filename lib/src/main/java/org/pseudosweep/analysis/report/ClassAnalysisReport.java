package org.pseudosweep.analysis.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.pseudosweep.analysis.Type;
import org.pseudosweep.program.CoverageElement;

import java.util.List;
import java.util.Set;


public class ClassAnalysisReport extends AnalysisReport {

    private final String packageName;
    private final String className;
    private Set<CoverageElement> covered;
    private Set<CoverageElement> effectualCovered;


    public ClassAnalysisReport(@JsonProperty("fileName") String fileName,
                               @JsonProperty("packageName") String packageName,
                               @JsonProperty("className") String className,
                               List<Type> metricsParams) {
        super(fileName, metricsParams);
        this.packageName = packageName;
        this.className = className;

    }


    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public Set<CoverageElement> getCovered() {
        return this.covered;
    }

    public void setCovered(Set<CoverageElement> covered) {
        this.covered = covered;
    }

    public Set<CoverageElement> getEffectualCovered() {
        return effectualCovered;
    }

    public void setEffectualCovered(Set<CoverageElement> allEffectualCovered) {
        this.effectualCovered = allEffectualCovered;
    }
}
