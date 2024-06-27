package org.pseudosweep;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.pseudosweep.program.CoverageElement;
import org.pseudosweep.program.comparator.CoverageElementComparator;
import org.pseudosweep.program.serialize.CoverageElementKeyDeserializer;
import org.pseudosweep.testframework.TestOutcome;

import java.lang.reflect.Method;
import java.util.*;

import static org.pseudosweep.util.StringUtils.millisAsHMSMString;

public class TestMethodReport {

    static final long MIN_MAX = 100;
    static final int TIMEOUT_MULTIPLIER = 2;

    private final String testClassName;
    private final String testMethodName;
    private List<TestOutcome> testOutcomes;

    @JsonDeserialize(keyUsing = CoverageElementKeyDeserializer.class)
    private Map<CoverageElement, List<TestOutcome>> coverageElementTestOutcomes;
    private long timeTaken;

    public TestMethodReport(@JsonProperty("testClassName") String testClassName,
                            @JsonProperty("testMethodName") String testMethodName) {
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        coverageElementTestOutcomes = new TreeMap<>(new CoverageElementComparator());
    }

    public TestMethodReport(Method testMethod, String runningClassName) {
        this(runningClassName, testMethod.getName());
    }

    public String getTestClassName() {
        return testClassName;
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public List<TestOutcome> getTestOutcomes() {
        return testOutcomes;
    }

    public void setTestOutcomes(List<TestOutcome> testOutcomes) {
        this.testOutcomes = testOutcomes;
    }

    @JsonIgnore
    public Set<CoverageElement> getCovered() {
        return coverageElementTestOutcomes.keySet();
    }

    @JsonIgnore
    public void setCovered(Set<CoverageElement> covered) {
        for (CoverageElement ce : covered) {
            coverageElementTestOutcomes.put(ce, null);
        }
    }

    @JsonIgnore
    public Set<CoverageElement> getEffectualCoverageTargets() {
        return new HashSet<>(coverageElementTestOutcomes.keySet());
    }

    public Map<CoverageElement, List<TestOutcome>> getCoverageElementTestOutcomes() {
        return coverageElementTestOutcomes;
    }

    public void setCoverageElementTestOutcomes(Map<CoverageElement, List<TestOutcome>> coverageElementTestOutcomes) {
        this.coverageElementTestOutcomes = coverageElementTestOutcomes;
    }

    @JsonIgnore
    public void setTargetTestOutcomesForCoverageElement(CoverageElement coverageElement, List<TestOutcome> testOutcomes) {
        coverageElementTestOutcomes.put(coverageElement, testOutcomes);
    }

    @JsonIgnore
    public List<TestOutcome> getTestOutcomesForCoverageElement(CoverageElement coverageElement) {
        return coverageElementTestOutcomes.get(coverageElement);
    }

    @JsonIgnore
    public Set<CoverageElement> getEffectualCovered() {
        Set<CoverageElement> effectualCovered = new HashSet<>();
        for (CoverageElement target : coverageElementTestOutcomes.keySet()) {
            if (coverageElementTestOutcomes.get(target) != null
                    && !coverageElementTestOutcomes.get(target).stream().allMatch(TestOutcome::passed)) {
                effectualCovered.add(target);
            }
        }
        return effectualCovered;
    }

    public boolean allInitialTestsPassed() {
        for (TestOutcome testOutcome : testOutcomes) {
            if (!testOutcome.passed()) {
                return false;
            }
        }
        return true;
    }

    public long computeTimeoutFromPassingInitialTests() {
        long max = MIN_MAX; // the minimum for max is set by MIN_MAX;
        for (TestOutcome testOutcome : testOutcomes) {
            if (testOutcome.passed()) {
                long runningTime = testOutcome.getRunTime();
                if (runningTime > max) {
                    max = runningTime;
                }
            }
        }
        return max * TIMEOUT_MULTIPLIER;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Test class:  ").append(testClassName).append("\n");
        out.append("Test method: ").append(testMethodName).append("\n");
        out.append("Time taken:  ").append(millisAsHMSMString(timeTaken)).append("\n");
        out.append("Test outcomes: ").append(testOutcomes).append("\n");

        List<CoverageElement> orderedCovered = new ArrayList<>(getCovered());
        orderedCovered.sort(new CoverageElementComparator());
        Set<CoverageElement> effectualCovered = getEffectualCovered();
        for (CoverageElement coverageElement : orderedCovered) {
            if (effectualCovered.contains(coverageElement)) {
                out.append("+");
            } else {
                out.append("-");
            }
            out.append(" ").append(coverageElement).append(" ");
            out.append(coverageElementTestOutcomes.get(coverageElement)).append("\n");
        }

        return out.toString();
    }
}
