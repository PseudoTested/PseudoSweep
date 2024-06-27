package org.pseudosweep;

import org.junit.jupiter.api.Test;
import org.pseudosweep.program.Block;
import org.pseudosweep.program.Decision;
import org.pseudosweep.program.Stmt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class AuditorTest {
    final boolean DEFAULT_VALUE = true;

    @Test
    public void fix_noTarget() {
        Decision trueDecision = new Decision(true, Decision.Type.IF, 1, "SomeClass");
        Decision falseDecision = new Decision(false, Decision.Type.IF, 1, "SomeClass");

        Auditor auditor = Auditor.executionCoverageAuditor();
        assertThat(auditor.fix(trueDecision), equalTo(true));
        assertThat(auditor.fix(falseDecision), equalTo(false));

        assertThat(auditor.getCovered().contains(trueDecision), equalTo(true));
        assertThat(auditor.getCovered().contains(falseDecision), equalTo(true));
    }

    @Test
    public void eval_noTarget() {
        Auditor auditor = Auditor.executionCoverageAuditor();
        assertThat(auditor.eval(), equalTo(true));
    }

    @Test
    public void exec_noTarget() {
        Stmt stmt = new Stmt(Stmt.Type.IF, 1, "SomeClass", true);

        Auditor auditor = Auditor.executionCoverageAuditor();
        assertThat(auditor.execSDL(stmt), equalTo(true));
        assertThat(auditor.getCovered().contains(stmt), equalTo(true));
    }

    @Test
    public void exec_target() {
        Stmt target = new Stmt(Stmt.Type.IF, 1, "SomeClass", true);
        Stmt executed1 = new Stmt(Stmt.Type.IF, 1, "SomeClass", true);
        Stmt executed2 = new Stmt(Stmt.Type.TRY, 2, "SomeClass", true);

        Auditor auditor = Auditor.effectualCoverageTargetAuditor(target);

        assertThat(auditor.execSDL(executed1), equalTo(false));
        assertThat(auditor.execSDL(executed2), equalTo(true));
    }

    @Test
    public void fix_target() {
        Decision trueDecision = new Decision(true, Decision.Type.IF, 1, "SomeClass");
        Decision falseDecision = new Decision(false, Decision.Type.IF, 1, "SomeClass");

        Auditor auditor = Auditor.effectualCoverageTargetAuditor(trueDecision);

        assertThat(auditor.fix(trueDecision), equalTo(false));
        assertThat(auditor.fix(falseDecision), equalTo(false));
    }

    @Test
    public void eval_target() {
        Decision trueDecision = new Decision(true, Decision.Type.IF, 1, "SomeClass");

        Auditor auditor = Auditor.effectualCoverageTargetAuditor(trueDecision);

        assertThat(auditor.eval(), equalTo(false));
    }

    @Test
    public void defaultValue_target() {
        Block target = new Block(Block.Type.METHOD, 1, "com.test.Test", DEFAULT_VALUE);
        Block b1 = new Block(Block.Type.METHOD, 1, "com.test.Test", !DEFAULT_VALUE);

        Auditor auditor = Auditor.effectualCoverageTargetAuditor(target);
        assertThat(auditor.execXMT(b1), equalTo(false));
        assertThat(auditor.defaultValueXMT(b1), equalTo(true));

    }

    @Test
    public void defaultValue_noTarget() {
        Block b1 = new Block(Block.Type.METHOD, 1, "com.test.Test", DEFAULT_VALUE);

        Auditor auditor = Auditor.executionCoverageAuditor();

        assertThat(auditor.defaultValueXMT(b1), equalTo(true));
    }
}

