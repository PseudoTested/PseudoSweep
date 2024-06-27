package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import org.pseudosweep.program.Decision;
import org.pseudosweep.program.Stmt;

public class InstrumentationCallGenerator {

    static final String EVAL_METHOD_NAME = "eval",
            FIX_METHOD_NAME = "fix",
            EXEC_METHOD_NAME = "exec",
            DEFAULT_VALUE_NAME = "defaultValue";
    static final FieldAccessExpr SCOPE = new FieldAccessExpr(
            new FieldAccessExpr(new NameExpr("org"), "pseudosweep"), "I");

    static Expression generateExpr(Expression expression, Decision.Type type, int decisionId, String containingClass) {
        return StaticJavaParser.parseExpression(generateFixPredicate(expression, type.toString(), decisionId, containingClass).toString());
    }

    static MethodCallExpr generateFixPredicate(Expression expression, String type, int decisionId, String containingClass) {
        MethodCallExpr methodCallExpr = new MethodCallExpr();
        methodCallExpr.setName(FIX_METHOD_NAME);
        methodCallExpr.setScope(SCOPE);
        NodeList<Expression> args = new NodeList<>();
        args.add(StaticJavaParser.parseExpression(generateEvalPredicate() + "&&" + expression));
        args.add(new StringLiteralExpr(type));
        args.add(new IntegerLiteralExpr(Integer.toString(decisionId)));
        args.add(new StringLiteralExpr(containingClass));
        methodCallExpr.setArguments(args);
        return methodCallExpr;
    }

    static MethodCallExpr generateEvalPredicate() {
        MethodCallExpr methodCallExpr = new MethodCallExpr();
        methodCallExpr.setName(EVAL_METHOD_NAME);
        methodCallExpr.setScope(SCOPE);
        return methodCallExpr;
    }

    static MethodCallExpr generateExecMethodCallExpr(Stmt.Type type, int id, String containingClass, String operatorSet) {
        return generateMethodCallExpr(EXEC_METHOD_NAME, type, id, containingClass, operatorSet);
    }


    private static MethodCallExpr generateMethodCallExpr(String methodName, Stmt.Type type, int id, String containingClass, String operatorSet) {
        return generateMethodCallExpr(methodName, id, containingClass, type.name(), operatorSet);
    }

    public static Expression generateDefaultValueCall(Stmt.Type blockType, int id, String containingClass, String operatorSet) {
        return generateMethodCallExpr(DEFAULT_VALUE_NAME, blockType, id, containingClass, operatorSet);
    }

    private static MethodCallExpr generateMethodCallExpr(String methodName, int id, String containingClass, String name, String operatorSet) {
        MethodCallExpr methodCallExpr = new MethodCallExpr();
        methodCallExpr.setName(methodName);
        methodCallExpr.setScope(SCOPE);
        NodeList<Expression> args = new NodeList<>();
        args.add(new StringLiteralExpr(name));
        args.add(new IntegerLiteralExpr(Integer.toString(id)));
        args.add(new StringLiteralExpr(containingClass));
        args.add(new StringLiteralExpr(operatorSet));
        methodCallExpr.setArguments(args);
        return methodCallExpr;
    }
}
