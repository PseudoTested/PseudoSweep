package org.pseudosweep.instrumentation.xmt;

import com.github.javaparser.Position;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import org.pseudosweep.instrumentation.DefaultValueGenerator;
import org.pseudosweep.program.Block;
import org.pseudosweep.program.ClassUnderTest;
import org.pseudosweep.program.SourceFilePosition;

public class BlockInstrumenter {

    private final ClassUnderTest classUnderTest;
    private final String operatorSet;
    private final Block blockDefaultTrue;
    private final Block blockDefaultFalse;
    private final int id;
    private final Block.Type blockType;
    private final String containingClass;

    BlockInstrumenter(Block.Type blockType, ClassUnderTest classUnderTest, String operatorSet) {
        this.classUnderTest = classUnderTest;
        this.id = generateID();
        this.blockType = blockType;
        this.containingClass = classUnderTest.getFullClassName();

        this.blockDefaultTrue = new Block(blockType, id, containingClass, true);
        this.blockDefaultFalse = new Block(blockType, id, containingClass, false);
        this.operatorSet = operatorSet;
        classUnderTest.addCoverageElement(blockDefaultTrue);
        classUnderTest.addCoverageElement(blockDefaultFalse);
    }

    private int generateID() {
        return classUnderTest.find(ce -> ce instanceof Block block && block.getDefaultSet()).size();
    }

    BlockStmt instrument(Statement statement) {
        return statement.isBlockStmt() ? instrument(statement.asBlockStmt()) : instrument(new BlockStmt(new NodeList<>(statement)));
    }

    BlockStmt instrument(NodeList<Statement> statements) {
        return instrument(new BlockStmt(statements));
    }

    BlockStmt instrument(BlockStmt blockStmt) {
        // Remove call to "super" or "this" prior to instrumentation, as it must remain the first line
        // of the constructor. (This is unavoidable.)
        Statement explicitConstructorInvocationStmt = removeExplicitConstructorInvocationStmt(blockStmt);

        BlockStmt instrumentedBlockStmt = new BlockStmt();
        if (containsStatements(blockStmt)) {
            IfStmt execStatement = new IfStmt();
            MethodCallExpr expression = InstrumentationCallGenerator.generateExecMethodCallExpr(
                    blockType, id, containingClass, operatorSet);
            execStatement.setCondition(expression);
            execStatement.setThenStmt(blockStmt);
            instrumentedBlockStmt.addStatement(execStatement);
            recordSourceFilePosition(blockDefaultTrue, blockStmt);
            recordSourceFilePosition(blockDefaultFalse, blockStmt);
        } else {
            // add a log instrumentation call to log that the block was executed, even if
            // there are no instructions in it to execute
            ExpressionStmt logStatement = InstrumentationCallGenerator.generateLogMethodCallStmt(
                    blockType, id, containingClass, operatorSet);
            instrumentedBlockStmt.addStatement(logStatement);
        }

        // Add super/this call back in, if it exists
        if (explicitConstructorInvocationStmt != null) {
            instrumentedBlockStmt.addStatement(0, explicitConstructorInvocationStmt);
        }

        return instrumentedBlockStmt;
    }

    static boolean containsStatements(BlockStmt blockStmt) {
        for (Statement statement : blockStmt.getStatements()) {
            if (!statement.isEmptyStmt() || !statement.isExplicitConstructorInvocationStmt()) {
                return true;
            }
        }
        return false;
    }

    static Statement removeExplicitConstructorInvocationStmt(BlockStmt blockStmt) {
        if (!blockStmt.isEmpty()) {
            Statement first = blockStmt.getStatement(0);
            if (first.isExplicitConstructorInvocationStmt()) {
                blockStmt.remove(first);
                return first;
            }
        }
        return null;
    }

    BlockStmt instrument(BlockStmt blockStmt, Type methodReturnType) {
        BlockStmt instrumentedBlockStmt = instrument(blockStmt);

        if (!methodReturnType.equals(new VoidType())) {
            Expression returnExpressionTrue = DefaultValueGenerator.defaultValueExpr(methodReturnType, true);
            Expression returnExpressionFalse = DefaultValueGenerator.defaultValueExpr(methodReturnType, false);

            ConditionalExpr conditionalExpr = new ConditionalExpr(InstrumentationCallGenerator.generateDefaultValueCall(blockType, id, containingClass, operatorSet), returnExpressionTrue, returnExpressionFalse);


            ReturnStmt returnStatement = new ReturnStmt(conditionalExpr);
            instrumentedBlockStmt.addStatement(returnStatement);
        }

        return instrumentedBlockStmt;
    }

    private void recordSourceFilePosition(Block block, BlockStmt blockStmt) {
        if (!blockStmt.isEmpty()) {
            NodeList<Statement> statements = blockStmt.getStatements();
            Statement first = statements.getFirst().get();
            Statement last = statements.getLast().get();

            if (first.getBegin().isPresent() && last.getEnd().isPresent()) {
                Position start = first.getBegin().get();
                Position end = last.getEnd().get();
                classUnderTest.setPosition(block, new SourceFilePosition(start.line, end.line, start.column, end.column));
            }
        }
    }
}
