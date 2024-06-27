package org.pseudosweep.analysis.report;

import org.pseudosweep.analysis.Type;

import java.util.List;

public class ProjectOverviewReport extends AnalysisReport {


    public ProjectOverviewReport(List<Type> metricsParams) {
        super("project-overview", metricsParams);
    }


}
