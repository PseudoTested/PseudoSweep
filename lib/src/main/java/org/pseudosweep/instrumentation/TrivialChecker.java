package org.pseudosweep.instrumentation;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.Objects;

public class TrivialChecker {

    public static boolean isTrivialMethod(MethodDeclaration node) {
        return
                isSimpleGetterMethod(node) ||
                isSimpleSetterMethod(node) ||
                isDeprecatedMethod(node) ||
                isEmptyVoidMethod(node) ||
                returnsConstantLiteralMethod(node) ||
                returnsEmptyInitializer(node) ||
                returnsOnlyThisMethod(node) ||
                returnsOnlyAParameter(node);
    }

    private static boolean isSimpleGetterMethod(MethodDeclaration node) {
        final String METHOD_NAME = node.getNameAsString();
        if (METHOD_NAME.startsWith("get") && node.getBody().isPresent()) {
            final BlockStmt METHOD_BODY = node.getBody().get();
            final int STATEMENT_COUNT = METHOD_BODY.getStatements().size();
            if (STATEMENT_COUNT == 1 && METHOD_BODY.getStatements().get(0).isReturnStmt()
                    && METHOD_BODY.getStatements().get(0).asReturnStmt().getExpression().isPresent()) {
                Expression ex = METHOD_BODY.getStatements().get(0).asReturnStmt().getExpression().get();
                return ex.isNameExpr() || ex.isFieldAccessExpr();
            }
        }
        return false;
    }

    private static boolean isSimpleSetterMethod(MethodDeclaration node) {
        final String METHOD_NAME = node.getNameAsString();
        if (METHOD_NAME.startsWith("set") && node.getBody().isPresent()) {
            final BlockStmt METHOD_BODY = node.getBody().get();
            final int STATEMENT_COUNT = METHOD_BODY.getStatements().size();
            return STATEMENT_COUNT == 1 && METHOD_BODY.getStatements().get(0).isExpressionStmt() && METHOD_BODY.getStatements().get(0).asExpressionStmt().getExpression().isAssignExpr();
        }
        return false;
    }

    private static boolean isDeprecatedMethod(MethodDeclaration node) {
        NodeList<AnnotationExpr> methodAnnotations = node.getAnnotations();
        for (AnnotationExpr annotation : methodAnnotations) {
            if (annotation.getNameAsString().equals("Deprecated")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEmptyVoidMethod(MethodDeclaration node) {
        return node.getType().isVoidType() && node.getBody().isEmpty();
    }

    private static boolean returnsConstantLiteralMethod(MethodDeclaration node) {
        if (node.getBody().isPresent()) {
            final BlockStmt METHOD_BODY = node.getBody().get();
            if (METHOD_BODY.getStatements().size() == 1 && METHOD_BODY.getStatement(0).isReturnStmt()) {
                ReturnStmt RETURN = METHOD_BODY.getStatement(0).asReturnStmt();
                return RETURN.getExpression().isPresent() && isLiteral(RETURN.getExpression().get());
            }
        }
        return false;
    }

    private static boolean isLiteral(Expression expression) {
        return expression.isLiteralExpr() || (expression.isUnaryExpr() && expression.asUnaryExpr().getExpression().isLiteralExpr()) || (expression.isCastExpr() && expression.asCastExpr().getExpression().isLiteralExpr());
    }

    private static boolean returnsEmptyInitializer(MethodDeclaration node) {
        if (node.getBody().isPresent()) {
            final BlockStmt METHOD_BODY = node.getBody().get();
            if (METHOD_BODY.getStatements().size() == 1 && METHOD_BODY.getStatement(0).isReturnStmt()) {
                ReturnStmt RETURN = METHOD_BODY.getStatement(0).asReturnStmt();
                return RETURN.getExpression().isPresent() && isEmptyArrayInitializer(RETURN.getExpression().get());
            }
        }
        return false;
    }

    private static boolean isEmptyArrayInitializer(Expression expression) {
        if (expression.isArrayCreationExpr() && expression.asArrayCreationExpr().getInitializer().isPresent()) {
            ArrayInitializerExpr arrayInitializerExpr = expression.asArrayCreationExpr().getInitializer().get();
            return arrayInitializerExpr.getValues().isEmpty();
        }
        return expression.isArrayCreationExpr() && expression.asArrayCreationExpr().getInitializer().isEmpty();
    }

    private static boolean returnsOnlyThisMethod(MethodDeclaration node) {
        if (node.getBody().isPresent()) {
            final BlockStmt METHOD_BODY = node.getBody().get();
            for (Statement statement : METHOD_BODY.getStatements()) {
                if (statement.isReturnStmt() && statement.asReturnStmt().getExpression().isPresent()) {
                    return statement.asReturnStmt().getExpression().get().isThisExpr();
                }
            }
        }
        return false;
    }

    private static boolean returnsOnlyAParameter(MethodDeclaration node) {
        if (node.getBody().isPresent()) {
            final BlockStmt METHOD_BODY = node.getBody().get();
            if (METHOD_BODY.getStatements().size() == 1 && METHOD_BODY.getStatement(0).isReturnStmt()) {
                final ReturnStmt RETURN = METHOD_BODY.getStatement(0).asReturnStmt();
                if (RETURN.getExpression().isPresent() && RETURN.getExpression().get().isNameExpr()) {
                    for (Parameter parameter : node.getParameters()) {
                        if (Objects.equals(RETURN.getExpression().get().asNameExpr().getNameAsString(), parameter.getNameAsString())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isDeprecatedClass(ClassOrInterfaceDeclaration node) {
        NodeList<AnnotationExpr> methodAnnotations = node.getAnnotations();
        for (AnnotationExpr annotation : methodAnnotations) {
            if (annotation.getNameAsString().equals("Deprecated")) {
                return true;
            }
        }
        return false;
    }
}
