package org.pseudosweep.instrumentation.xmt;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import org.junit.jupiter.api.Test;
import org.pseudosweep.program.Block;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class InstrumentationCallGeneratorTest {
    final String OPERATOR_SET = "xmt";
    final boolean DEFAULT_VALUE_SET = true;


    @Test
    public void generateExecCall() {
        Block block = new Block(Block.Type.METHOD, 10, "com.test.Test", DEFAULT_VALUE_SET);
        Expression call = InstrumentationCallGenerator.generateExecMethodCallExpr(block.getType(), block.getId(), block.getContainingClass(), OPERATOR_SET);

        assertThat(call.toString(), equalTo("org.pseudosweep.I.exec(\"METHOD\", 10, \"com.test.Test\", \"xmt\")"));
    }

    @Test
    public void generateLogCall() {
        Block block = new Block(Block.Type.METHOD, 10, "com.test.Test", DEFAULT_VALUE_SET);
        ExpressionStmt expressionStmt = InstrumentationCallGenerator.generateLogMethodCallStmt(block.getType(), block.getId(), block.getContainingClass(), OPERATOR_SET);
        assertThat(expressionStmt.toString(), equalTo("org.pseudosweep.I.log(\"METHOD\", 10, \"com.test.Test\", \"xmt\");"));
    }

    @Test
    public void generateDefaultValueCall_true() {
        Block block = new Block(Block.Type.METHOD, 10, "com.test.Test", DEFAULT_VALUE_SET);
        ExpressionStmt expressionStmt = new ExpressionStmt(InstrumentationCallGenerator.generateDefaultValueCall(block.getType(), block.getId(), block.getContainingClass(), OPERATOR_SET));
        assertThat(expressionStmt.toString(), equalTo("org.pseudosweep.I.defaultValue(\"METHOD\", 10, \"com.test.Test\", \"xmt\");"));
    }

    @Test
    public void generateDefaultValueCall_false() {
        Block block = new Block(Block.Type.METHOD, 10, "com.test.Test", false);
        ExpressionStmt expressionStmt = new ExpressionStmt(InstrumentationCallGenerator.generateDefaultValueCall(block.getType(), block.getId(), block.getContainingClass(), OPERATOR_SET));
        assertThat(expressionStmt.toString(), equalTo("org.pseudosweep.I.defaultValue(\"METHOD\", 10, \"com.test.Test\", \"xmt\");"));
    }
}
