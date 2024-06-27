package org.pseudosweep.instrumentation.xmt;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import org.pseudosweep.program.Block;

public class InstrumentationCallGenerator {

    static final String EXEC_METHOD_NAME = "exec",
            LOG_METHOD_NAME = "log",
            DEFAULT_VALUE_NAME = "defaultValue";

    static final FieldAccessExpr SCOPE = new FieldAccessExpr(
            new FieldAccessExpr(new NameExpr("org"), "pseudosweep"), "I");


    static MethodCallExpr generateExecMethodCallExpr(Block.Type type, int id, String containingClass, String operatorSet) {
        return generateBlockMethodCallExpr(EXEC_METHOD_NAME, type, id, containingClass, operatorSet);
    }

    static ExpressionStmt generateLogMethodCallStmt(Block.Type type, int id, String containingClass, String operatorSet) {
        MethodCallExpr methodCallExpr = generateBlockMethodCallExpr(LOG_METHOD_NAME, type, id, containingClass, operatorSet);
        ExpressionStmt expressionStmt = new ExpressionStmt();
        expressionStmt.setExpression(methodCallExpr);
        return expressionStmt;
    }

    public static Expression generateDefaultValueCall(Block.Type blockType, int id, String containingClass, String operatorSet) {
        return generateBlockMethodCallExpr(DEFAULT_VALUE_NAME, blockType, id, containingClass, operatorSet);
    }

    private static MethodCallExpr generateBlockMethodCallExpr(String methodName, Block.Type type, int id, String containingClass, String operatorSet) {
        MethodCallExpr methodCallExpr = new MethodCallExpr();
        methodCallExpr.setName(methodName);
        methodCallExpr.setScope(SCOPE);
        NodeList<Expression> args = new NodeList<>();
        args.add(new StringLiteralExpr(type.name()));
        args.add(new IntegerLiteralExpr(Integer.toString(id)));
        args.add(new StringLiteralExpr(containingClass));
        args.add(new StringLiteralExpr(operatorSet));
        methodCallExpr.setArguments(args);
        return methodCallExpr;
    }


}
