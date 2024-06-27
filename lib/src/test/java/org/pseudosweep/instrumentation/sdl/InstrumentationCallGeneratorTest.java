package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import org.junit.jupiter.api.Test;
import org.pseudosweep.program.Decision;
import org.pseudosweep.program.Stmt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

class InstrumentationCallGeneratorTest {

    final String OPERATOR_SET = "sdl";

    @Test
    public void generateExecCall() {
        Stmt stmt = new Stmt(Stmt.Type.EXPRESSION, 10, "com.test.Test", true);
        Expression call = InstrumentationCallGenerator.generateExecMethodCallExpr(stmt.getType(), stmt.getId(), stmt.getContainingClass(), OPERATOR_SET);
        assertThat(call.toString(), equalTo("org.pseudosweep.I.exec(\"EXPRESSION\", 10, \"com.test.Test\", \"sdl\")"));
    }

    @Test
    public void generateFixCall() {
        BooleanLiteralExpr expression = new BooleanLiteralExpr(true);
        Decision decision = new Decision(true, Decision.Type.LOOP, 10, "com.test.Test");
        Expression call = InstrumentationCallGenerator.generateExpr(expression, decision.getType(), decision.getId(), decision.getContainingClass());
        assertThat(call.toString(), equalTo("org.pseudosweep.I.fix(org.pseudosweep.I.eval() && true, \"LOOP\", 10, \"com.test.Test\")"));
    }

}
