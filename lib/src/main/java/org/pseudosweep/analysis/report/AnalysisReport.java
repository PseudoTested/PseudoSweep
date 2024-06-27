package org.pseudosweep.analysis.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.pseudosweep.analysis.Metrics;
import org.pseudosweep.analysis.Type;

import java.util.HashMap;
import java.util.List;

public abstract class AnalysisReport {

    private final String fileName;

    private final HashMap<Type, Metrics> typeMetricsHashMap;
    private final List<Type> typeList;

    @JsonPropertyOrder({"totalMutantCount", "typeMetricsHashMap"})
    public AnalysisReport(@JsonProperty("fileName") String fileName, List<Type> typeList) {
        this.fileName = fileName;
        typeMetricsHashMap = new HashMap<>();
        this.typeList = typeList;
        typeList.forEach(type -> typeMetricsHashMap.put(type, new Metrics()));
    }

    public String getFileName() {
        return fileName;
    }


    public int getTotalElementCount() {
        int sum = 0;
        for (Type type : typeList) {
            if (typeMetricsHashMap.get(type) != null) {
                sum += typeMetricsHashMap.get(type).getElementCount();

            }
        }
        return sum;
    }

    public int getTotalCoveredCount() {
        int sum = 0;
        for (Type type : typeList) {
            if (typeMetricsHashMap.get(type) != null) {
                sum += typeMetricsHashMap.get(type).getCoveredCount();
            } else {
                System.out.println(type);
            }
        }
        return sum;
    }

    public int getTotalEffectualCount() {
        int sum = 0;
        for (Type type : typeList) {
            if (typeMetricsHashMap.get(type) != null) {
                sum += typeMetricsHashMap.get(type).getEffectualCount();
            }
        }
        return sum;
    }

    public int getTotalCoverageGap() {
        int sum = 0;
        for (Type type : typeList) {
            if (typeMetricsHashMap.get(type) != null) {
                sum += typeMetricsHashMap.get(type).getCoverageGapSize();
            }
        }
        return sum;
    }

    public HashMap<Type, Metrics> getTypeMetricsHashMap() {
        return typeMetricsHashMap;
    }

    public void setTypeMetricsHashMap(Type type, Metrics metrics) {
        this.typeMetricsHashMap.put(type, metrics);
    }

}
