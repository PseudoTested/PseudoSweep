package org.pseudosweep.instrumentation.xmt;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import org.pseudosweep.program.Block;
import org.pseudosweep.program.ClassUnderTest;
import org.pseudosweep.util.JavaConstants;

import java.util.List;
import java.util.Set;

import static org.pseudosweep.instrumentation.TrivialChecker.isDeprecatedClass;
import static org.pseudosweep.instrumentation.TrivialChecker.isTrivialMethod;
import static org.pseudosweep.instrumentation.xmt.FinalModifierRemover.removeFinalModifiers;
import static org.pseudosweep.program.Block.Type.METHOD;

public class SourceFileInstrumenter extends org.pseudosweep.instrumentation.SourceFileInstrumenter {


    public SourceFileInstrumenter(String fileName, CompilationUnit compilationUnit, String operatorSet, boolean skipTrivial) {
        super(fileName, compilationUnit, operatorSet, skipTrivial);
    }

    public String instrument() {
        compilationUnit.accept(new SourceFileModifierVisitor(), null);
        removeFinalModifiers(compilationUnit);
        return compilationUnit.toString();
    }

    ClassUnderTest getCurrentClassUnderTest() {
        return classesBeingParsed.peek();
    }

    public Set<ClassUnderTest> getClassesParsed() {
        return classesParsed;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    private class SourceFileModifierVisitor extends ModifierVisitor<Void> {

        private Visitable newClass(TypeDeclaration<?> node) {
            String className = node.getName().toString();

            // set the new name of the containingClass
            if (!classesBeingParsed.empty()) {
                className = getCurrentClassUnderTest().getClassName() + JavaConstants.CLASS_SEPARATOR + className;
            }

            ClassUnderTest currentClassUnderTest = new ClassUnderTest(fileName, packageName, className);
            classesBeingParsed.push(currentClassUnderTest);

            // visit children
            for (Node child : node.getChildNodes()) {
                child.accept(this, null);
            }

            // finished parsing, move the ClassUnderTest off the stack and into the parsed set
            classesParsed.add(classesBeingParsed.pop());

            return node;
        }

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration node, Void arg) {
            if (skipTrivial && isDeprecatedClass(node)) {
                return node;
            }
            return newClass(node);
        }


        @Override
        public Visitable visit(ConstructorDeclaration node, Void arg) {
            BlockInstrumenter blockInstrumenter = new BlockInstrumenter(Block.Type.CONSTRUCTOR, getCurrentClassUnderTest(), operatorSet);

            super.visit(node, null); 

            BlockStmt instrumented = blockInstrumenter.instrument(node.getBody());
            node.setBody(instrumented);

            return node;
        }


        @Override
        public Visitable visit(EnumDeclaration node, Void arg) {
            return newClass(node);
        }


        @Override
        public Visitable visit(InitializerDeclaration node, Void arg) {
            if (skipTrivial && node.isStatic() || node.getBody().isEmpty()) {
                return node;
            }
            BlockInstrumenter blockInstrumenter = new BlockInstrumenter(Block.Type.INITIALIZER, getCurrentClassUnderTest(), operatorSet);

            super.visit(node, null); 

            BlockStmt instrumentedBody = blockInstrumenter.instrument(node.getBody());
            node.setBody(instrumentedBody);

            return node;
        }


        @Override
        public Visitable visit(MethodDeclaration node, Void arg) {
            if (skipTrivial && isTrivialMethod(node)) {
                return node;
            }
            if (node.getBody().isPresent()) {
                BlockInstrumenter blockInstrumenter = new BlockInstrumenter(METHOD, getCurrentClassUnderTest(), operatorSet);
                super.visit(node, null); 

                BlockStmt instrumentedBody = blockInstrumenter.instrument(node.getBody().get(), node.getType());
                node.setBody(instrumentedBody);
            }
            return node;
        }

        @Override
        public Visitable visit(PackageDeclaration node, Void arg) {
            packageName = node.getName().asString();
            super.visit(node, null); 
            return node;
        }
    }
}
