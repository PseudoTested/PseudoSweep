package org.pseudosweep.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import org.pseudosweep.DataManager;
import org.pseudosweep.TestMethodReport;
import org.pseudosweep.analysis.Analysis;
import org.pseudosweep.analysis.sdl.TypeSDL;
import org.pseudosweep.analysis.xmt.TypeXMT;
import org.pseudosweep.program.ClassUnderTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.io.File.separator;

@Parameters(commandDescription = " >>> analyzes effectual coverage for each class")
public class AnalyzeCommand extends Command {
    private String operatorSet;

    @Parameter(names = {"-me", "--typesToCover"},
            description = "Output only the overage for specified metrics for sweep results")
    private List<String> typesToCover = new ArrayList<>();


    @Override
    public String getName() {
        return "analyze";
    }

    @Override
    public void checkParams() throws ParameterException {
        if (!(statementDeletion || extremeMutation)) {
            throw new ParameterException("One of --statementdeletion (-sdl) or --extrememutation (-xmt) must be used");
        }
        operatorSet = (statementDeletion) ? "sdl" : "xmt";
        if (typesToCover.isEmpty() && operatorSet.equals("sdl")) {
            Arrays.asList(TypeSDL.values()).forEach(type -> typesToCover.add(type.toString()));
        } else if (typesToCover.isEmpty()) {
            Arrays.asList(TypeXMT.values()).forEach(type -> typesToCover.add(type.toString()));
        }

    }

    @Override
    void run() {
        final String CLASSES_DIRECTORY_NAME = "classes-" + operatorSet;
        final String RESULTS_DIRECTORY_NAME = "results-" + operatorSet;
        
        final String CLASS_UNDER_TEST_SET_DATA_PATH = dataPath + separator + CLASSES_DIRECTORY_NAME + separator;
        Set<ClassUnderTest> classUnderTestSet = DataManager.deserializeClassesUnderTest(CLASS_UNDER_TEST_SET_DATA_PATH);

        final String TEST_METHOD_REPORT_SET_DATA_PATH = dataPath + separator + RESULTS_DIRECTORY_NAME + separator;
        Set<TestMethodReport> testMethodReportSet = DataManager.deserializeTestMethodReports((TEST_METHOD_REPORT_SET_DATA_PATH));
        
        Analysis analysis = new Analysis(testMethodReportSet, typesToCover, operatorSet);
        analysis.processAnalysis(classUnderTestSet);

    }

}

