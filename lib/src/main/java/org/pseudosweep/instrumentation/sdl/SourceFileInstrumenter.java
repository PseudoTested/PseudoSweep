package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import org.pseudosweep.instrumentation.sdl.TempFinalInsertion.InnerClassVariableUsageVisitor;
import org.pseudosweep.program.ClassUnderTest;
import org.pseudosweep.program.Stmt;
import org.pseudosweep.util.JavaConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.pseudosweep.instrumentation.TrivialChecker.isDeprecatedClass;
import static org.pseudosweep.instrumentation.TrivialChecker.isTrivialMethod;
import static org.pseudosweep.instrumentation.sdl.DeclarationInstrumenter.splitDeclarations;
import static org.pseudosweep.instrumentation.sdl.FinalModifierRemover.removeFinalModifiers;
import static org.pseudosweep.instrumentation.sdl.ReturnMutatorInstrumenter.addReturnValueMutator;
import static org.pseudosweep.instrumentation.sdl.SwitchEntryInstrumenter.addDefaultAssignments;
import static org.pseudosweep.instrumentation.sdl.TempFinalInsertion.addTempFinals;
import static org.pseudosweep.program.Decision.Type.IF;
import static org.pseudosweep.program.Decision.Type.LOOP;

public class SourceFileInstrumenter extends org.pseudosweep.instrumentation.SourceFileInstrumenter {


    public SourceFileInstrumenter(String fileName, CompilationUnit compilationUnit, String operatorSet, boolean skipTrivial) {
        super(fileName, compilationUnit, operatorSet, skipTrivial);
    }

    public String instrument() {
        this.compilationUnit.accept(new SourceFileModifierVisitor(), null);
        removeFinalModifiers(compilationUnit);
        addTempFinals(compilationUnit);
        addReturnValueMutator(compilationUnit);
        return compilationUnit.toString();
    }

    void addWarning(Node node, String message) {
        if (node.getBegin().isPresent()) {
            message = "Line " + node.getBegin().get().line + ": " + message;
        }
        warnings.add(message);
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


    class SourceFileModifierVisitor extends ModifierVisitor<Void> {

        
        private Visitable newClass(TypeDeclaration<?> node) {
            String className = node.getName().toString();

            if (!classesBeingParsed.empty()) {
                className = getCurrentClassUnderTest().getClassName() + JavaConstants.CLASS_SEPARATOR + className;
            }

            ClassUnderTest currentClassUnderTest = new ClassUnderTest(fileName, packageName, className);
            classesBeingParsed.push(currentClassUnderTest);

            for (Node child : node.getChildNodes()) {
                child.accept(this, null);
            }

            classesParsed.add(classesBeingParsed.pop());

            return node;
        }

        @Override
        public Visitable visit(ArrayInitializerExpr node, Void arg) {
            super.visit(node, null); 
            if (node.getParentNode().isPresent() && node.getParentNode().get() instanceof VariableDeclarator vd) {
                return StaticJavaParser.parseExpression("new " + vd.getType() + node);
            }

            return node;
        }

        @Override
        public Visitable visit(BlockStmt node, Void arg) {
            super.visit(node, null); 
            node.setStatements(splitDeclarations(node.getStatements(), getCurrentClassUnderTest(), operatorSet));
            return node;
        }

        public Visitable visit(BreakStmt node, Void arg) {
            StmtInstrumenter stmtInstrumenter = new StmtInstrumenter(Stmt.Type.BREAK, getCurrentClassUnderTest(), operatorSet);
            super.visit(node, null);
            return stmtInstrumenter.instrument(node);
        }


        @Override
        public Visitable visit(ClassOrInterfaceDeclaration node, Void arg) {
            if (skipTrivial && isDeprecatedClass(node)) {
                return node;
            }
            return newClass(node);
        }

        public Visitable visit(ContinueStmt node, Void arg) {
            StmtInstrumenter stmtInstrumenter = new StmtInstrumenter(Stmt.Type.CONTINUE, getCurrentClassUnderTest(), operatorSet);
            super.visit(node, null);
            return stmtInstrumenter.instrument(node);
        }
        
        @Override
        public Visitable visit(DoStmt node, Void arg) {
            
            StmtInstrumenter stmtInstrumenter = new StmtInstrumenter(Stmt.Type.DO, getCurrentClassUnderTest(), operatorSet);
            DecisionInstrumenter predicateI = new DecisionInstrumenter(LOOP, getCurrentClassUnderTest());

            super.visit(node, null); 

            Expression expression = node.getCondition();
            Expression instrumentedExpression = predicateI.instrument(expression);
            node.setCondition(instrumentedExpression);

            return stmtInstrumenter.instrument(node);
        }

        
        @Override
        public Visitable visit(EnumDeclaration node, Void arg) {
            return newClass(node);
        }

        @Override
        public Visitable visit(ExpressionStmt node, Void arg) {

            if (node.getExpression().isAssignExpr()
                    && node.getExpression().asAssignExpr().getValue().isConditionalExpr()
                    && node.getExpression().asAssignExpr().getValue().asConditionalExpr().getCondition().toString().contains("SWITCH_ENTRY_ASSIGNMENT")) {
                return node;
            }

            super.visit(node, null);

            if (node.getExpression().isVariableDeclarationExpr())
                return node;

            List<NameExpr> lambdaContainsVariableUses = new ArrayList<>();
            node.accept(new TempFinalInsertion.LambdaVariableUsageVisitor(), lambdaContainsVariableUses);
            List<NameExpr> innerClassContainsVariableUses = new ArrayList<>();
            node.accept(new InnerClassVariableUsageVisitor(), innerClassContainsVariableUses);

            StmtInstrumenter stmtInstrumenter = getInstrumenter(lambdaContainsVariableUses, innerClassContainsVariableUses);

            return stmtInstrumenter.instrument(node);
        }

        private StmtInstrumenter getInstrumenter(List<NameExpr> lambdaContainsVariableUses, List<NameExpr> innerClassContainsVariableUses) {
            StmtInstrumenter stmtInstrumenter;
            if (!lambdaContainsVariableUses.isEmpty()) {
                stmtInstrumenter = new StmtInstrumenter(Stmt.Type.LAMBDA, getCurrentClassUnderTest(), operatorSet);
            } else if (!innerClassContainsVariableUses.isEmpty()) {
                stmtInstrumenter = new StmtInstrumenter(Stmt.Type.INNER_CLASS, getCurrentClassUnderTest(), operatorSet);
            } else {
                stmtInstrumenter = new StmtInstrumenter(Stmt.Type.EXPRESSION, getCurrentClassUnderTest(), operatorSet);
            }
            return stmtInstrumenter;
        }

        @Override
        public Visitable visit(InitializerDeclaration node, Void arg) {
            if (skipTrivial && node.isStatic()) {
                return node;
            }
            super.visit(node, arg);
            return node;
        }

        @Override
        public Visitable visit(IfStmt node, Void arg) {
            if (node.getCondition().toString().contains("org.pseudosweep.I.exec(\"VARIABLE_DECLARATION\"")) {
                return node;
            }

            StmtInstrumenter fullStatementSI = new StmtInstrumenter(Stmt.Type.IF, getCurrentClassUnderTest(), operatorSet);
            DecisionInstrumenter predicateI = new DecisionInstrumenter(IF, getCurrentClassUnderTest());

            super.visit(node, null); 

            Expression expression = node.getCondition();
            Expression instrumentedExpression = predicateI.instrument(expression);
            node.setCondition(instrumentedExpression);

            return fullStatementSI.instrument(node);
        }

        @Override
        public Visitable visit(ForStmt node, Void arg) {
            StmtInstrumenter stmtInstrumenter = new StmtInstrumenter(Stmt.Type.FOR, getCurrentClassUnderTest(), operatorSet);
            DecisionInstrumenter decisionInstrumenter = new DecisionInstrumenter(LOOP, getCurrentClassUnderTest());

            super.visit(node, null); 

            if (node.getCompare().isPresent()) {
                node.setCompare(decisionInstrumenter.instrument(node.getCompare().get()));
            }

            return stmtInstrumenter.instrument(node);
        }
        
        @Override
        public Visitable visit(ForEachStmt node, Void arg) {
            StmtInstrumenter fullStatementSI = new StmtInstrumenter(Stmt.Type.FOR_EACH, getCurrentClassUnderTest(), operatorSet);

            super.visit(node, null); 

            return fullStatementSI.instrument(node);
        }

        public Visitable visit(LabeledStmt node, Void arg) {
            StmtInstrumenter fullStatementSI;

            if (node.getStatement().isWhileStmt()) {
                fullStatementSI = new StmtInstrumenter(Stmt.Type.WHILE, getCurrentClassUnderTest(), operatorSet);
                super.visit(node.getStatement().asWhileStmt(), null);
            } else if (node.getStatement().isForStmt()) {
                fullStatementSI = new StmtInstrumenter(Stmt.Type.FOR, getCurrentClassUnderTest(), operatorSet);
                super.visit(node.getStatement().asForStmt(), null);
            } else if (node.getStatement().isForEachStmt()) {
                fullStatementSI = new StmtInstrumenter(Stmt.Type.FOR_EACH, getCurrentClassUnderTest(), operatorSet);
                super.visit(node.getStatement().asForEachStmt(), null);
            } else if (node.getStatement().isDoStmt()) {
                fullStatementSI = new StmtInstrumenter(Stmt.Type.DO, getCurrentClassUnderTest(), operatorSet);
                super.visit(node.getStatement().asDoStmt(), null);
            } else {
                addWarning(node, "Unrecognised labelled node");
                return node;
            }

            return fullStatementSI.instrument(node);
        }

        @Override
        public Visitable visit(LambdaExpr node, Void arg) {
            return node;
        }

        @Override
        public Visitable visit(MethodDeclaration node, Void arg) {
            if (skipTrivial && isTrivialMethod(node)) {
                return node;
            }

            if (node.getBody().isPresent()) {
                super.visit(node, null); 
            }
            return node;
        }

        @Override
        public Visitable visit(PackageDeclaration node, Void arg) {
            packageName = node.getName().asString();
            super.visit(node, null); 
            return node;
        }

        @Override
        public Visitable visit(ReturnStmt node, Void arg) {

            super.visit(node, null); 

            List<NameExpr> lambdaContainsVariableUses = new ArrayList<>();
            node.accept(new TempFinalInsertion.LambdaVariableUsageVisitor(), lambdaContainsVariableUses);
            List<NameExpr> innerClassContainsVariableUses = new ArrayList<>();
            node.accept(new InnerClassVariableUsageVisitor(), innerClassContainsVariableUses);


            StmtInstrumenter stmtInstrumenter = getStmtInstrumenter(lambdaContainsVariableUses, innerClassContainsVariableUses);

            return stmtInstrumenter.instrument(node);
        }

        private StmtInstrumenter getStmtInstrumenter(List<NameExpr> lambdaContainsVariableUses, List<NameExpr> innerClassContainsVariableUses) {
            StmtInstrumenter stmtInstrumenter;
            if (!lambdaContainsVariableUses.isEmpty()) {
                stmtInstrumenter = new StmtInstrumenter(Stmt.Type.LAMBDA_RETURN, getCurrentClassUnderTest(), operatorSet);
            } else if (!innerClassContainsVariableUses.isEmpty()) {
                stmtInstrumenter = new StmtInstrumenter(Stmt.Type.INNER_CLASS_RETURN, getCurrentClassUnderTest(), operatorSet);
            } else {
                stmtInstrumenter = new StmtInstrumenter(Stmt.Type.RETURN, getCurrentClassUnderTest(), operatorSet);
            }
            return stmtInstrumenter;
        }

        @Override
        public Visitable visit(SwitchStmt node, Void arg) {
            StmtInstrumenter stmtInstrumenter = new StmtInstrumenter(Stmt.Type.SWITCH, getCurrentClassUnderTest(), operatorSet);
            addDefaultAssignments(node, getCurrentClassUnderTest(), operatorSet);

            super.visit(node, null); 

            return stmtInstrumenter.instrument(node);
        }

        @Override
        public Visitable visit(SynchronizedStmt node, Void arg) {
            return node;
        }

        @Override
        public Visitable visit(ThrowStmt node, Void arg) {
            StmtInstrumenter stmtInstrumenter = new StmtInstrumenter(Stmt.Type.THROW, getCurrentClassUnderTest(), operatorSet);

            super.visit(node, null); 

            return stmtInstrumenter.instrument(node);
        }
        
        @Override
        public Visitable visit(TryStmt node, Void arg) {
            StmtInstrumenter stmtInstrumenter = new StmtInstrumenter(Stmt.Type.TRY, getCurrentClassUnderTest(), operatorSet);

            super.visit(node, null); 

            return stmtInstrumenter.instrument(node);
        }

        @Override
        public Visitable visit(WhileStmt node, Void arg) {
            
            StmtInstrumenter fullStatementSI = new StmtInstrumenter(Stmt.Type.WHILE, getCurrentClassUnderTest(), operatorSet);
            DecisionInstrumenter predicateI = new DecisionInstrumenter(LOOP, getCurrentClassUnderTest());

            super.visit(node, null); 

            Expression expression = node.getCondition();
            Expression instrumentedExpression = predicateI.instrument(expression);
            node.setCondition(instrumentedExpression);

            return fullStatementSI.instrument(node);
        }

    }
}
