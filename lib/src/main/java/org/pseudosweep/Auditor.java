package org.pseudosweep;

import org.pseudosweep.logging.LogManager;
import org.pseudosweep.logging.Logger;
import org.pseudosweep.program.Block;
import org.pseudosweep.program.BooleanExpression;
import org.pseudosweep.program.CoverageElement;
import org.pseudosweep.program.Stmt;

import java.util.HashSet;
import java.util.Set;

import static org.pseudosweep.util.StringUtils.encloseParentheses;
import static org.pseudosweep.util.StringUtils.threadInfoString;

public final class Auditor {

    private static final Logger logger = LogManager.getLogger(Auditor.class);

    private final Set<CoverageElement> covered;
    private final boolean logExecutionCoverage;
    private final CoverageElement effectualCoverageTarget;

    private boolean cancel;

    private Auditor(boolean logExecutionCoverage, CoverageElement effectualCoverageTarget) {
        this.logExecutionCoverage = logExecutionCoverage;
        this.effectualCoverageTarget = effectualCoverageTarget;
        covered = new HashSet<>();
        cancel = false;
    }

    static Auditor defaultAuditor() {
        return new Auditor(false, null);
    }

    static Auditor executionCoverageAuditor() {
        return new Auditor(true, null);
    }

    static Auditor effectualCoverageTargetAuditor(CoverageElement effectualCoverageTarget) {
        return new Auditor(false, effectualCoverageTarget);
    }

    boolean hasTarget() {
        return effectualCoverageTarget != null;
    }

    void cancel() {
        cancel = true;
    }

    Set<CoverageElement> getCovered() {
        return covered;
    }

    public void log(CoverageElement element) {
        checkCancellation();
        if (logExecutionCoverage) {
            if (element instanceof Block b) {
                Block defaultBlock = new Block(b.getType(), b.getId(), b.getContainingClass(), b.getDefaultSet());
                Block notDefaultBlock = new Block(b.getType(), b.getId(), b.getContainingClass(), !b.getDefaultSet());

                covered.add(defaultBlock);
                covered.add(notDefaultBlock);

            } else if (element instanceof Stmt s) {
                Stmt defaultStmt = new Stmt(s.getType(), s.getId(), s.getContainingClass(), s.getDefaultSet());
                Stmt notDefaultStmt = new Stmt(s.getType(), s.getId(), s.getContainingClass(), !s.getDefaultSet());

                covered.add(defaultStmt);
                covered.add(notDefaultStmt);
            } else {
                covered.add(element);
            }
        }

    }

    public boolean fix(BooleanExpression booleanExpression) {
        checkCancellation();

        if (hasTarget()) {
            boolean invertTruthValue = booleanExpression.equals(effectualCoverageTarget);
            boolean truthValue = booleanExpression.getTruthValue();

            return invertTruthValue != truthValue;
        } else {
            log(booleanExpression);
            return booleanExpression.getTruthValue();
        }
    }

    public boolean eval() {
        checkCancellation();
        return !hasTarget();
    }

    public boolean execSDL(Stmt stmt) {
        checkCancellation();

        if (hasTarget()) {
            return !stmt.equalsNoDefault(effectualCoverageTarget);
        } else {
            log(stmt);
            return true;
        }
    }

    public boolean defaultValueSDL(Stmt stmt) {
        checkCancellation();

        if (hasTarget() && stmt.equalsNoDefault(effectualCoverageTarget)) {
            return ((Stmt) effectualCoverageTarget).getDefaultSet();
        } else {
            log(stmt);
            return true;
        }

    }

    private void checkCancellation() {
        if (cancel) {
            logger.trace("Forcing test thread to end " + encloseParentheses(threadInfoString(Thread.currentThread())));
            throw new TestCancelledException();
        }
    }

    public boolean execXMT(Block block) {
        checkCancellation();

        if (hasTarget()) {
            return !block.equalsNoDefault(effectualCoverageTarget);
        } else {
            log(block);
            return true;
        }
    }

    public boolean defaultValueXMT(Block block) {
        checkCancellation();

        if (hasTarget() && block.equalsNoDefault(effectualCoverageTarget)) {
            return ((Block) effectualCoverageTarget).getDefaultSet();
        } else {
            log(block);
            return true;
        }

    }
}
