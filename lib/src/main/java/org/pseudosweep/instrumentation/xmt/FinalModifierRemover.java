package org.pseudosweep.instrumentation.xmt;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithFinalModifier;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;


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

        @Override
        public Visitable visit(FieldDeclaration node, Void arg) {
            removeFinalForVariableDeclarationWithNoInitializer(node);
            return node;
        }

    }
}
