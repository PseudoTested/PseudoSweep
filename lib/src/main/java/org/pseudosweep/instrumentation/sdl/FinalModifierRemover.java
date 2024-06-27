package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithFinalModifier;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.pseudosweep.instrumentation.DefaultValueGenerator.defaultValueExpr;


public class FinalModifierRemover {

    public static void removeFinalModifiers(CompilationUnit cu) {
        cu.accept(new DeclarationVisitor(), null);
    }

    private static class DeclarationVisitor extends ModifierVisitor<Void> {
        static <T extends NodeWithVariables<?> & NodeWithFinalModifier<?>> void removeFinalForVariableDeclarationWithNoInitializer(T node) {
            for (VariableDeclarator variable : node.getVariables()) {
                if (variable.getInitializer().isEmpty()) {
                    node.setFinal(false);
                    return;
                }
            }
        }

        static <T extends NodeWithVariables<?> & NodeWithFinalModifier<?>> void removeFinalForVariableDeclarationWithInitializer(T node) {
            node.setFinal(false);
        }

        @Override
        public Visitable visit(FieldDeclaration node, Void arg) {
            removeFinalForVariableDeclarationWithNoInitializer(node);
            return node;
        }

        @Override
        public Visitable visit(VariableDeclarationExpr node, Void arg) {
            removeFinalForVariableDeclarationWithNoInitializer(node);
            
            AtomicBoolean hasInitializerBefore = new AtomicBoolean(false);

            node.getVariables().forEach(variableDeclarator ->
                    hasInitializerBefore.set(variableDeclarator.getInitializer().isPresent()));

            super.visit(node, null); 

            AtomicBoolean hasInitializerAfter = new AtomicBoolean(false);
            node.getVariables().forEach(variableDeclarator ->
                    hasInitializerAfter.set(variableDeclarator.getInitializer().isPresent()));

            if(hasInitializerBefore.getPlain() != hasInitializerAfter.getPlain()){
                removeFinalForVariableDeclarationWithInitializer(node);
            }

            return node;
        }


        @Override
        public Visitable visit(VariableDeclarator node, Void arg) {
            super.visit(node, null); 

            Optional<Expression> expression = node.getInitializer();
            if (expression.isEmpty()) {

                boolean addExpression = true;

                if (node.getParentNode().get() instanceof VariableDeclarationExpr vde) {
                    if (vde.getParentNode().get() instanceof ForEachStmt) {
                        addExpression = false;
                    }
                }

                if (addExpression) {
                    node.setInitializer(defaultValueExpr(node.getType(), true));
                }
            }

            return node;
        }
    }
}
