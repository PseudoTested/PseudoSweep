package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import static org.pseudosweep.instrumentation.sdl.StmtInstrumenter.addDefaultReturn;
import static org.pseudosweep.instrumentation.sdl.StmtInstrumenter.endOfMethodDefaultReturnCheck;

public class ReturnMutatorInstrumenter {


    public static void addReturnValueMutator(CompilationUnit cu) {
        cu.accept(new MethodVisitor(), null);
    }

    private static class MethodVisitor extends ModifierVisitor<Void> {
        @Override
        public Visitable visit(MethodDeclaration node, Void arg) {
            super.visit(node, arg);
            node.accept(new BlockVisitor(), node.getType());
            if(node.getBody().isPresent())
                node.setBody(endOfMethodDefaultReturnCheck(node.getBody().get(), node.getType()));
            return node;
        }

        private static class BlockVisitor extends ModifierVisitor<Type> {


            @Override
            public Visitable visit(BlockStmt node, Type type) {
                super.visit(node, type);

                addDefaultReturn(node, type);
                return node;
            }
        }
    }
}
