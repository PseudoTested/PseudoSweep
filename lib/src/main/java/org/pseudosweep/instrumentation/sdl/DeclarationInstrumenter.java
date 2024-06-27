package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.pseudosweep.program.ClassUnderTest;
import org.pseudosweep.program.Stmt;

import java.util.ArrayList;
import java.util.List;

import static org.pseudosweep.instrumentation.DefaultValueGenerator.defaultValueExpr;


public class DeclarationInstrumenter {

    static NodeList<Statement> splitDeclarations(NodeList<Statement> nodeList, ClassUnderTest currentClassUnderTest, String operatorSet) {
        BlockStmt newNode = new BlockStmt();
        for (Statement currentStatement : nodeList) {
            if (currentStatement.isExpressionStmt() && currentStatement.asExpressionStmt().getExpression().isVariableDeclarationExpr()) {

                VariableDeclarationExpr vde = currentStatement.asExpressionStmt().getExpression().asVariableDeclarationExpr();
                if (!vde.isFinal()) {
                    for (VariableDeclarator variableDeclarator : vde.getVariables()) {

                        if (variableDeclarator.getInitializer().isPresent()) {

                            // Add instrumented statements to new node
                            getInstrumentedStatements(currentClassUnderTest,
                                    operatorSet,
                                    currentStatement,
                                    variableDeclarator
                            ).forEach(newNode::addStatement);

                        } else {
                            // Separate variable declarations into separate expressions
                            VariableDeclarationExpr variableDeclarationExpr = new VariableDeclarationExpr(variableDeclarator);
                            newNode.addStatement(new ExpressionStmt(variableDeclarationExpr));
                        }
                    }
                } else newNode.addStatement(currentStatement);
            } else newNode.addStatement(currentStatement);
        }
        nodeList = newNode.getStatements();
        return nodeList;
    }

    private static List<Statement> getInstrumentedStatements(ClassUnderTest currentClassUnderTest, String operatorSet, Statement currentStatement, VariableDeclarator variableDeclarator) {
        List<Statement> statements = new ArrayList<>();

        // Check for lambdas
        List<NameExpr> lambdaContainsVariableUses = new ArrayList<>();
        currentStatement.accept(new TempFinalInsertion.LambdaVariableUsageVisitor(), lambdaContainsVariableUses);
        // Check for inner classes
        List<NameExpr> innerClassContainsVariableUses = new ArrayList<>();
        currentStatement.accept(new TempFinalInsertion.InnerClassVariableUsageVisitor(), innerClassContainsVariableUses);

        StmtInstrumenter stmtInstrumenter;
        VariableDeclarationExpr variableDeclarationExpr = new VariableDeclarationExpr(variableDeclarator.clone().setInitializer(defaultValueExpr(variableDeclarator.getType(), true)));

        if (!lambdaContainsVariableUses.isEmpty()) {
            // Lambda variable declaration
            stmtInstrumenter = new StmtInstrumenter(Stmt.Type.LAMBDA, currentClassUnderTest, operatorSet);
            statements.add(new ExpressionStmt(variableDeclarationExpr));
            statements.add(stmtInstrumenter.instrumentInnerClassOrLambdaVarDec(currentStatement, variableDeclarator));

        } else if (!innerClassContainsVariableUses.isEmpty()) {
            // Inner Class variable declaration
            stmtInstrumenter = new StmtInstrumenter(Stmt.Type.INNER_CLASS, currentClassUnderTest, operatorSet);
            statements.add(new ExpressionStmt(variableDeclarationExpr));
            statements.add(stmtInstrumenter.instrumentInnerClassOrLambdaVarDec(currentStatement, variableDeclarator));

        } else { // All other Variable declarations

            stmtInstrumenter = new StmtInstrumenter(Stmt.Type.VARIABLE_DECLARATION, currentClassUnderTest, operatorSet);
            Statement newVariableInitialization = stmtInstrumenter.instrumentDeclaration(variableDeclarator);
            variableDeclarationExpr = new VariableDeclarationExpr(variableDeclarator.setInitializer(defaultValueExpr(variableDeclarator.getType(), true)));
            statements.add(new ExpressionStmt(variableDeclarationExpr));
            statements.add(newVariableInitialization);

        }
        return statements;
    }


}
