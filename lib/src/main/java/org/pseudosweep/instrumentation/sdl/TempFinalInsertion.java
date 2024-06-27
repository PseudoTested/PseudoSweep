package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;

public class TempFinalInsertion {

    private static final String TEMP_FINAL_SUFFIX = "_pseudosweep";

    public static void addTempFinals(CompilationUnit cu) {
        cu.accept(new BlockVisitor(), null);
    }

    private static ExpressionStmt getTempFinal(VariableDeclarator vd) {
        ExpressionStmt temp_final = new ExpressionStmt();
        VariableDeclarator temp_final_vd = new VariableDeclarator(vd.getType(),
                vd.getNameAsString() + TEMP_FINAL_SUFFIX,
                vd.getNameAsExpression());
        VariableDeclarationExpr temp_final_vde = new VariableDeclarationExpr();
        temp_final_vde.addVariable(temp_final_vd);
        temp_final_vde.setFinal(true);
        temp_final.setExpression(temp_final_vde);

        return temp_final;
    }

    private static ExpressionStmt reassignTempFinal(VariableDeclarator vd) {
        ExpressionStmt expressionStmt = new ExpressionStmt();
        AssignExpr assignExpr = new AssignExpr(vd.getNameAsExpression(),
                new NameExpr(vd.getNameAsString() + TEMP_FINAL_SUFFIX),
                AssignExpr.Operator.ASSIGN);
        expressionStmt.setExpression(assignExpr);
        return expressionStmt;
    }

    private static class BlockVisitor extends ModifierVisitor<Void> {

        @Override
        public Visitable visit(BlockStmt node, Void arg) {
            Map<VariableDeclarator, Statement> variablesToLookFor = new HashMap<>();

            for (int i = 0; i < node.getStatements().size(); i++) {
                Statement innerStatement = node.getStatement(i);
                // identify variables that may require the temp final variables
                if (innerStatement instanceof ExpressionStmt ex
                        && ex.getExpression().isVariableDeclarationExpr()
                        && !ex.getExpression().asVariableDeclarationExpr().isFinal()) {

                    VariableDeclarationExpr vde = ex.getExpression().asVariableDeclarationExpr();
                    for (VariableDeclarator vd : vde.getVariables()) {
                        variablesToLookFor.put(vd, null);
                    }

                }

                // Look for assignments and uses of variable.
                Set<VariableDeclarator> varNames = variablesToLookFor.keySet();
                innerStatement.accept(new VariableUsageModifierVisitor(), varNames);
            }

            super.visit(node, arg);
            return node;
        }

    }

    private static class VariableUsageModifierVisitor extends ModifierVisitor<Set<VariableDeclarator>> {

        private static IfStmt addTemporaryVariables(IfStmt node,
                                                    IfStmt originalNode,
                                                    Set<VariableDeclarator> variablesToAssign,
                                                    Set<VariableDeclarator> variablesToReassign) {
            // add temporary variables if node has been modified
            if (!node.equals(originalNode)) {
                BlockStmt thenStmt = new BlockStmt();
                for (VariableDeclarator variableDeclarator : variablesToAssign) {
                    if (!variableDeclarator.getNameAsString().endsWith(TEMP_FINAL_SUFFIX)) {
                        thenStmt.addStatement(getTempFinal(variableDeclarator));
                    }
                }
                if (node.getThenStmt().isBlockStmt()) {
                    for (Statement statement : node.getThenStmt().asBlockStmt().getStatements()) {
                        thenStmt.addStatement(statement);
                    }
                } else {
                    thenStmt.addStatement(node.getThenStmt());
                }
                if (!variablesToReassign.isEmpty()) {
                    for (VariableDeclarator variableDeclarator : variablesToReassign) {
                        if (variableDeclarator.getNameAsString().endsWith(TEMP_FINAL_SUFFIX)) {
                            continue;
                        }
                        thenStmt.addStatement(reassignTempFinal(variableDeclarator));
                    }
                }

                node.setThenStmt(thenStmt);
            }
            return node;
        }

        @Override
        public Visitable visit(IfStmt node, Set<VariableDeclarator> variablesToLookFor) {
            if (node.getCondition().isMethodCallExpr() &&
                    (node.getCondition().asMethodCallExpr().getArguments().contains(new StringLiteralExpr("LAMBDA_RETURN")))) {
                IfStmt originalNode = node.clone();
                node.accept(new LambdaVariableModifyingVisitor(), variablesToLookFor);
                return addTemporaryVariables(node, originalNode, variablesToLookFor, new HashSet<>());
            }
            if (node.getCondition().isMethodCallExpr() &&
                    (node.getCondition().asMethodCallExpr().getArguments().contains(new StringLiteralExpr("LAMBDA")))) {
                Set<VariableDeclarator> nodesToReassign = new HashSet<>(variablesToLookFor);
                IfStmt originalNode = node.clone();
                node.accept(new LambdaVariableModifyingVisitor(), variablesToLookFor);
                node.accept(new LambdaVariableModifyingVisitor(), nodesToReassign);
                return addTemporaryVariables(node, originalNode, variablesToLookFor, nodesToReassign);
            }

            if (node.getCondition().isMethodCallExpr() &&
                    node.getCondition().asMethodCallExpr().getArguments().contains(new StringLiteralExpr("INNER_CLASS_RETURN"))) {
                IfStmt originalNode = node.clone();
                node.accept(new InnerClassVariableModifyingVisitor(), variablesToLookFor);
                return addTemporaryVariables(node, originalNode, variablesToLookFor, new HashSet<>());
            }
            if (node.getCondition().isMethodCallExpr() &&
                    node.getCondition().asMethodCallExpr().getArguments().contains(new StringLiteralExpr("INNER_CLASS"))) {
                Set<VariableDeclarator> nodesToReassign = new HashSet<>(variablesToLookFor);
                IfStmt originalNode = node.clone();
                node.accept(new InnerClassVariableModifyingVisitor(), variablesToLookFor);
                node.accept(new InnerClassVariableModifyingVisitor(), nodesToReassign);
                return addTemporaryVariables(node, originalNode, variablesToLookFor, nodesToReassign);
            }

            super.visit(node, variablesToLookFor);

            return node;
        }

        public static class LambdaVariableModifyingVisitor extends ModifierVisitor<Set<VariableDeclarator>> {

            @Override
            public Visitable visit(BlockStmt node, Set<VariableDeclarator> variablesToLookFor) {
                node.accept(new VariableUsageVisitor(), variablesToLookFor);

                super.visit(node, variablesToLookFor);

                return node;
            }


            private static class VariableUsageVisitor extends ModifierVisitor<Set<VariableDeclarator>> {

                @Override
                public Visitable visit(AssignExpr node, Set<VariableDeclarator> varNames) {
                    Set<VariableDeclarator> nodesToRemove = new HashSet<>();

                    for (VariableDeclarator varName : varNames) {
                        if (Objects.equals(node.getTarget().toString(), varName.getNameAsString())) {
                            nodesToRemove.add(varName);
                        }
                    }

                    nodesToRemove.forEach(varNames::remove);
                    super.visit(node, varNames);

                    return node;
                }

                @Override
                public Visitable visit(NameExpr node, Set<VariableDeclarator> varNames) {

                    super.visit(node, varNames);

                    Set<VariableDeclarator> variablesUsed = new HashSet<>();
                    for (VariableDeclarator varName : varNames) {
                        if (Objects.equals(node.getNameAsString(), varName.getNameAsString())) {
                            node.setName(node.getNameAsString() + TEMP_FINAL_SUFFIX);
                            variablesUsed.add(varName);
                        }
                    }
                    varNames = variablesUsed;
                    return node;
                }
            }
        }

        public static class InnerClassVariableModifyingVisitor extends ModifierVisitor<Set<VariableDeclarator>> {

            @Override
            public Visitable visit(BlockStmt node, Set<VariableDeclarator> variablesToLookFor) {
                node.accept(new VariableUsageVisitor(), variablesToLookFor);

                super.visit(node, variablesToLookFor);

                return node;
            }


            private static class VariableUsageVisitor extends ModifierVisitor<Set<VariableDeclarator>> {

                @Override
                public Visitable visit(AssignExpr node, Set<VariableDeclarator> varNames) {
                    Set<VariableDeclarator> nodesToRemove = new HashSet<>();

                    for (VariableDeclarator varName : varNames) {
                        if (Objects.equals(node.getTarget().toString(), varName.getNameAsString())) {
                            nodesToRemove.add(varName);
                        }
                    }

                    nodesToRemove.forEach(varNames::remove);
                    super.visit(node, varNames);

                    return node;
                }

                @Override
                public Visitable visit(NameExpr node, Set<VariableDeclarator> varNames) {

                    super.visit(node, varNames);

                    Set<VariableDeclarator> variablesUsed = new HashSet<>();
                    for (VariableDeclarator varName : varNames) {
                        if (Objects.equals(node.getNameAsString(), varName.getNameAsString())) {
                            node.setName(node.getNameAsString() + TEMP_FINAL_SUFFIX);
                            variablesUsed.add(varName);
                        }
                    }

                    varNames = variablesUsed;
                    return node;
                }

            }
        }
    }

    public static class LambdaVariableUsageVisitor extends VoidVisitorAdapter<List<NameExpr>> {
        @Override
        public void visit(LambdaExpr node, List<NameExpr> varNames) {
            node.accept(new VariableUsageVisitor(), varNames);
            super.visit(node, varNames);
        }


        private static class VariableUsageVisitor extends VoidVisitorAdapter<List<NameExpr>> {
            @Override
            public void visit(NameExpr node, List<NameExpr> varNames) {
                varNames.add(node);
            }
        }
    }

    public static class InnerClassVariableUsageVisitor extends VoidVisitorAdapter<List<NameExpr>> {
        @Override
        public void visit(ClassOrInterfaceDeclaration node, List<NameExpr> varNames) {
            node.accept(new InnerClassVariableUsageVisitor.VariableUsageVisitor(), varNames);
            super.visit(node, varNames);
        }

        @Override
        public void visit(MethodDeclaration node, List<NameExpr> varNames) {
            node.accept(new InnerClassVariableUsageVisitor.VariableUsageVisitor(), varNames);
            super.visit(node, varNames);
        }


        private static class VariableUsageVisitor extends VoidVisitorAdapter<List<NameExpr>> {
            @Override
            public void visit(NameExpr node, List<NameExpr> varNames) {
                varNames.add(node);
            }

        }
    }
}
