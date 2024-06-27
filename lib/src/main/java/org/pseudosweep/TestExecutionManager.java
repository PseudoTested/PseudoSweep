package org.pseudosweep;

import org.pseudosweep.logging.LogManager;
import org.pseudosweep.logging.Logger;
import org.pseudosweep.program.CoverageElement;
import org.pseudosweep.testframework.TestOutcome;
import org.pseudosweep.testframework.TestRunners;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.pseudosweep.util.StringUtils.threadInfoString;

public class TestExecutionManager {

    private static final Logger logger = LogManager.getLogger(TestExecutionManager.class);

    private static TestExecutionManager instance;

    private final Thread mainThread;
    private final Map<Thread, Auditor> auditors;
    private Auditor currentAuditor;
    private long testTimeout;
    private int testRepeats;

    TestExecutionManager() {
        auditors = new ConcurrentHashMap<>();
        mainThread = Thread.currentThread();
        registerAuditor(mainThread, Auditor.defaultAuditor());
    }

    public static TestExecutionManager instance() {
        if (instance == null) {
            instance = new TestExecutionManager();
        }
        return instance;
    }

    public void setTestTimeout(long testTimeout) {
        this.testTimeout = testTimeout;
    }

    public void setTestRepeats(int testRepeats) {
        this.testRepeats = testRepeats;
    }

    public Auditor getAuditor() {
        Thread currentThread = Thread.currentThread();

        Auditor auditor = auditors.get(currentThread);

        if (auditor == null) {
            auditor = currentAuditor;
            registerAuditor(currentThread, auditor);
        }

        return auditor;
    }

    public void registerAuditor(Thread thread, Auditor auditor) {
        logger.trace("Registering current auditor with " + threadInfoString(thread));
        auditors.put(thread, auditor);
    }

    public void findCoverage(TestMethodReport report, Method testMethod, Class<?> testClass, CoverageElement effectualCoverageTarget) {
        boolean findExecutionCoverage = effectualCoverageTarget == null;
        boolean findEffectualCoverage = effectualCoverageTarget != null;

        List<TestOutcome> testOutcomes = new ArrayList<>();
        Set<CoverageElement> allCovered = new HashSet<>();

        for (int i = 0; i < testRepeats; i++) {
            Auditor auditor = findExecutionCoverage ?
                    Auditor.executionCoverageAuditor() :
                    Auditor.effectualCoverageTargetAuditor(effectualCoverageTarget);
            logger.trace("Set current auditor to: " + auditor);
            currentAuditor = auditor;
            TestOutcome testOutcome = (mainThread == Thread.currentThread()) ? TestRunners.executeTest(testMethod, testClass, testTimeout) : new TestOutcome(TestOutcome.Type.SKIPPED, 0);
            testOutcomes.add(testOutcome);
            auditor.cancel();
            cleanUpThreads();

            logger.trace("Test outcome: " + testOutcome);

            if (!testOutcome.passed()) {
                break;
            }

            if (findExecutionCoverage) {
                allCovered.addAll(auditor.getCovered());
            }
        }

        if (findExecutionCoverage) {
            report.setTestOutcomes(testOutcomes);
            report.setCovered(allCovered);
        }

        if (findEffectualCoverage) {
            report.setTargetTestOutcomesForCoverageElement(effectualCoverageTarget, testOutcomes);
        }
    }

    void cleanUpThreads() {
        for (Thread thread : auditors.keySet()) {
            if (!thread.equals(mainThread)) {
                if (thread.isAlive()) {
                    logger.trace("Killing " + threadInfoString(thread));
                    thread.interrupt();
                } else {
                    auditors.remove(thread);
                }
            }
        }
    }
}

