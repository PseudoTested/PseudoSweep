package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.pseudosweep.program.ClassUnderTest;
import org.pseudosweep.program.Stmt;

import java.util.ArrayList;
import java.util.List;

import static org.pseudosweep.instrumentation.sdl.DeclarationInstrumenter.splitDeclarations;

public class SwitchEntryInstrumenter {

    static ClassUnderTest classUnderTest;
    static String operatorSet;

    static SwitchStmt addDefaultAssignments(SwitchStmt node, ClassUnderTest currentClassUnderTest, String operatorSet) {
        classUnderTest = currentClassUnderTest;
        SwitchEntryInstrumenter.operatorSet = operatorSet;
        List<VariableDeclarationExpr> declarations = new ArrayList<>();

        for (SwitchEntry switchEntry : node.getEntries()) {
            switchEntry.setStatements(splitDeclarations(switchEntry.getStatements(), currentClassUnderTest, operatorSet));

            // check for uses of declarations from previous SwitchEntries
            switchEntry.accept(new AssignmentVisitor(), declarations);
            // add declarations from current statement to list
            declarations.addAll(getDeclarations(switchEntry));
        }

        return node;
    }


    private static List<VariableDeclarationExpr> getDeclarations(SwitchEntry switchEntry) {

        List<VariableDeclarationExpr> declarations = new ArrayList<>();

        VoidVisitorAdapter<List<VariableDeclarationExpr>> declarationCollector = new SwitchEntryVisitor();
        declarationCollector.visit(switchEntry, declarations);

        return declarations;
    }

    private static AssignExpr getConditionalExpression(AssignExpr node, VariableDeclarator variableDeclarator) {
        StmtInstrumenter stmtInstrumenter = new StmtInstrumenter(Stmt.Type.SWITCH_ENTRY_ASSIGNMENT, classUnderTest, operatorSet);
        return stmtInstrumenter.instrument(node, variableDeclarator);
    }

    private static class AssignmentVisitor extends ModifierVisitor<List<VariableDeclarationExpr>> {

        public Visitable visit(AssignExpr node, List<VariableDeclarationExpr> previousDeclarations) {
            super.visit(node, previousDeclarations);

            // if the assign target is in the previous declarations list,
            // turn the assignment into ternary expression with default value.
            for (VariableDeclarationExpr varDecExpr : previousDeclarations) {
                for (VariableDeclarator variable : varDecExpr.getVariables()) {
                    if (node.getTarget().equals(variable.getNameAsExpression())) {
                        node = getConditionalExpression(node, variable);
                    }
                }
            }

            return node;
        }

    }

    private static class SwitchEntryVisitor extends VoidVisitorAdapter<List<VariableDeclarationExpr>> {

        @Override
        public void visit(VariableDeclarationExpr node, List<VariableDeclarationExpr> declarations) {
            super.visit(node, declarations);
            declarations.add(node);
        }

    }


}

