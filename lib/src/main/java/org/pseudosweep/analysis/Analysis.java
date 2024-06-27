package org.pseudosweep.analysis;

import org.pseudosweep.DataManager;
import org.pseudosweep.TestMethodReport;
import org.pseudosweep.analysis.report.ClassAnalysisReport;
import org.pseudosweep.analysis.report.ProjectOverviewReport;
import org.pseudosweep.analysis.sdl.TypeSDL;
import org.pseudosweep.analysis.xmt.TypeXMT;
import org.pseudosweep.command.AnalyzeCommand;
import org.pseudosweep.logging.LogManager;
import org.pseudosweep.logging.Logger;
import org.pseudosweep.program.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.pseudosweep.analysis.sdl.TypeSDL.DECISION;

public class Analysis {

    private static final Logger logger = LogManager.getLogger(AnalyzeCommand.class);
    private final String operatorSet;

    protected ProjectOverviewReport projectOverviewReport;

    protected Set<CoverageElement> allCovered, allEffectualCovered, allIneffectualCovered;
    List<Type> typesList;

    public Analysis(Set<TestMethodReport> testMethodReportSet, List<String> parameters, String operatorSet) {

        this.typesList = getTypeSet(parameters, operatorSet);
        this.operatorSet = operatorSet;
        collateCoverageElements(testMethodReportSet);
        projectOverviewReport = new ProjectOverviewReport(typesList);
    }

    private static Set<CoverageElement> getElementsFromClass(Set<CoverageElement> elements, Set<CoverageElement> allCovered) {
        Set<CoverageElement> covered = new HashSet<>(elements);
        covered.retainAll(allCovered);
        return covered;
    }

    protected static Set<CoverageElement> getElementsOfType(ClassUnderTest classUnderTest, Type type) {
        Set<CoverageElement> elements = new HashSet<>();

        for (CoverageElement element : classUnderTest.getCoverageElements()) {

            if ((type.equals(DECISION) && (element instanceof Decision))
                    || ((element instanceof Stmt s) && s.getType().toString().equals(type.toString()))
                    || ((element instanceof Block b) && b.getType().toString().equals(type.toString()))) {
                elements.add(element);
            }

        }
        return elements;
    }

    private List<Type> getTypeSet(List<String> parameters, String operatorSet) {
        List<Type> types = new ArrayList<>();
        switch (operatorSet) {
            case "sdl" -> {
                for (String p : parameters) {
                    types.add(TypeSDL.valueOf(p));
                }
            }
            case "xmt" -> {
                for (String p : parameters) {
                    types.add(TypeXMT.valueOf(p));
                }
            }
        }
        return types;
    }

    protected ClassAnalysisReport addMetrics(ClassUnderTest classUnderTest, ClassAnalysisReport classAnalysisReport) {

        for (Type type : typesList) {
            Metrics metrics = new Metrics();
            classAnalysisReport.setTypeMetricsHashMap(type, calculateValues(classUnderTest, type, metrics));
        }

        return classAnalysisReport;
    }

    protected ProjectOverviewReport addMetrics(ClassUnderTest classUnderTest, ProjectOverviewReport projectOverviewReport) {

        for (Type type : typesList) {

            Metrics metrics = projectOverviewReport.getTypeMetricsHashMap().get(type);

            projectOverviewReport.setTypeMetricsHashMap(type, calculateValues(classUnderTest, type, metrics));
        }

        return projectOverviewReport;
    }

    public void processAnalysis(Set<ClassUnderTest> classUnderTestSet) {
        int classNo = 0;
        final int TOTAL_CLASS_NO = classUnderTestSet.size();

        for (ClassUnderTest classUnderTest : classUnderTestSet) {

            ClassAnalysisReport classAnalysisReport = createClassAnalysisReport(classUnderTest);

            classAnalysisReport = addMetrics(classUnderTest, classAnalysisReport);
            classAnalysisReport.setCovered(getElementsFromClass(classUnderTest.getCoverageElements(), allCovered));
            classAnalysisReport.setEffectualCovered(getElementsFromClass(classUnderTest.getCoverageElements(), allEffectualCovered));

            DataManager.serializeClassAnalysisReport(classAnalysisReport, operatorSet);

            projectOverviewReport = addMetrics(classUnderTest, projectOverviewReport);

            classNo++;
            final String CLASS_FULL_NAME = classUnderTest.getFullClassName();
            logger.info(classNo + "/" + TOTAL_CLASS_NO + ": " + CLASS_FULL_NAME);
        }

        DataManager.serializeAnalysisReport(projectOverviewReport, operatorSet);
    }

    private ClassAnalysisReport createClassAnalysisReport(ClassUnderTest classUnderTest) {
        final String FILE_NAME = classUnderTest.getFileName();
        final String PACKAGE_NAME = classUnderTest.getPackageName();
        final String CLASS_NAME = classUnderTest.getClassName();

        return new ClassAnalysisReport(FILE_NAME, PACKAGE_NAME, CLASS_NAME, typesList);
    }

    private Metrics calculateValues(ClassUnderTest classUnderTest, Type type, Metrics metrics) {
        Set<CoverageElement> deletions = getElementsOfType(classUnderTest, type);

        final int ELEMENT_COUNT = deletions.size();

        final int COVERED_COUNT = getElementsFromClass(deletions, allCovered).size();

        final int EFFECTUAL_COUNT = getElementsFromClass(deletions, allEffectualCovered).size();

        Set<CoverageElement> survivingMutants = new HashSet<>(deletions);
        survivingMutants.retainAll(allIneffectualCovered);

        metrics.updateMetrics(ELEMENT_COUNT, COVERED_COUNT, EFFECTUAL_COUNT, survivingMutants);
        return metrics;
    }

    protected void collateCoverageElements(Set<TestMethodReport> testMethodReportSet) {
        allCovered = new HashSet<>();
        allEffectualCovered = new HashSet<>();
        allIneffectualCovered = new HashSet<>();

        for (TestMethodReport testMethodReport : testMethodReportSet) {

            Set<CoverageElement> newCovered = testMethodReport.getCovered();
            Set<CoverageElement> newEffectual = testMethodReport.getEffectualCovered();

            allCovered.addAll(newCovered);
            allEffectualCovered.addAll(newEffectual);
        }
        allIneffectualCovered = identifyIneffectual(allCovered, allEffectualCovered);
    }

    private Set<CoverageElement> identifyIneffectual(Set<CoverageElement> covered, Set<CoverageElement> effectual) {
        Set<CoverageElement> ineffectual = new HashSet<>(covered);
        ineffectual.removeAll(effectual);
        return ineffectual;
    }
}

