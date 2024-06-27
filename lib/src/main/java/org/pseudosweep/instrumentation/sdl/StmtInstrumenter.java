package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.Position;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;
import org.pseudosweep.program.ClassUnderTest;
import org.pseudosweep.program.SourceFilePosition;
import org.pseudosweep.program.Stmt;

import java.util.Optional;

import static org.pseudosweep.instrumentation.DefaultValueGenerator.defaultValueExpr;
import static org.pseudosweep.instrumentation.sdl.InstrumentationCallGenerator.generateDefaultValueCall;
import static org.pseudosweep.instrumentation.sdl.InstrumentationCallGenerator.generateExecMethodCallExpr;
import static org.pseudosweep.program.Stmt.Type.RETURN;

public class StmtInstrumenter {

    static final boolean DEFAULT_VALUE = true;
    static final String QUOTE = "\"",
            EMPTY_STRING = "",
            RESWEEP_EXEC_CALL_NAME = "exec",
            RESWEEP_DEFAULT_VALUE_CALL_NAME = "defaultValue",
            RETURN_KEYWORD = "RETURN";
    private final Stmt stmtDefaultTrue, stmtDefaultFalse;
    private final ClassUnderTest classUnderTest;
    private final String operatorSet, containingClass;
    private final Stmt.Type statementType;
    private final int id;

    StmtInstrumenter(Stmt.Type statementType, ClassUnderTest classUnderTest, String operatorSet) {
        this.classUnderTest = classUnderTest;
        this.statementType = statementType;
        this.id = generateID();
        this.containingClass = classUnderTest.getFullClassName();
        this.stmtDefaultTrue = new Stmt(this.statementType, this.id, containingClass, true);
        this.stmtDefaultFalse = new Stmt(this.statementType, this.id, containingClass, false);
        this.operatorSet = operatorSet;

        classUnderTest.addCoverageElement(stmtDefaultTrue);
        if (statementType == RETURN) {
            classUnderTest.addCoverageElement(stmtDefaultFalse);
        }
    }

    public static BlockStmt addDefaultReturn(BlockStmt blockStmt, final Type METHOD_RETURN_TYPE) {

        if (hasReturn(METHOD_RETURN_TYPE)) {

            if (lastStatementIsADefaultValueExpr(blockStmt) || lastStatementIsASyncStmt(blockStmt)) {
                return blockStmt;
            }

            for (Statement statement : blockStmt.getStatements()) {
                if (statement.isIfStmt() && statement.asIfStmt().getCondition().isMethodCallExpr()) {
                    final MethodCallExpr MCE = statement.asIfStmt().getCondition().asMethodCallExpr();
                    final NodeList<Expression> args = MCE.getArguments();

                    if (isAReSweepMethodCall(MCE) && isAnInstrumentedReturn(args)) {

                        final String RETURN_ID = args.get(1).toString();
                        final String RETURN_CONTAINING_CLASS = args.get(2).toString().replace(QUOTE, EMPTY_STRING);
                        final String RETURN_OPERATOR_SET = args.get(3).toString().replace(QUOTE, EMPTY_STRING);

                        final Expression RETURN_EXPRESSION_TRUE = defaultValueExpr(METHOD_RETURN_TYPE, true);
                        final Expression RETURN_EXPRESSION_FALSE = defaultValueExpr(METHOD_RETURN_TYPE, false);
                        final Expression DEFAULT_VALUE_METHOD_CALL = generateDefaultValueCall(
                                RETURN,
                                Integer.parseInt(RETURN_ID),
                                RETURN_CONTAINING_CLASS, RETURN_OPERATOR_SET);

                        final ConditionalExpr returnExpression = new ConditionalExpr(
                                DEFAULT_VALUE_METHOD_CALL,
                                RETURN_EXPRESSION_TRUE,
                                RETURN_EXPRESSION_FALSE);
                        final ReturnStmt returnStatement = new ReturnStmt(returnExpression);
                        blockStmt.addStatement(returnStatement);
                        return blockStmt;
                    }
                }
            }

        }
        return blockStmt;

    }

    private static boolean hasReturn(Type METHOD_RETURN_TYPE) {
        boolean hasReturn = !METHOD_RETURN_TYPE.isVoidType();

        // Check if a void method has been mis-parsed - JAVA-PARSER BUG
        if (METHOD_RETURN_TYPE.getComment().isPresent() && METHOD_RETURN_TYPE.getElementType().toString().endsWith("void")) {
            hasReturn = false;
        }
        return hasReturn;
    }

    protected static BlockStmt endOfMethodDefaultReturnCheck(BlockStmt blockStmt, Type METHOD_RETURN_TYPE) {
        if (hasReturn(METHOD_RETURN_TYPE)) {

            if (lastStatementIsADefaultValueExpr(blockStmt) || lastStatementIsASyncStmt(blockStmt)) {
                return blockStmt;
            }

            final int BLOCK_SIZE = blockStmt.getStatements().size();
            if (BLOCK_SIZE > 0) {
                Statement lastStatement = blockStmt.getStatement(BLOCK_SIZE - 1);

                if (lastStatement.isIfStmt() && lastStatement.asIfStmt().getCondition().isMethodCallExpr()) {

                    final MethodCallExpr MCE = lastStatement.asIfStmt().getCondition().asMethodCallExpr();

                    if (isAReSweepMethodCall(MCE)) {
                        Expression returnExpression = defaultValueExpr(METHOD_RETURN_TYPE, DEFAULT_VALUE);
                        ReturnStmt returnStatement = new ReturnStmt(returnExpression);
                        blockStmt.addStatement(returnStatement);
                    }
                } else if (lastStatement.isSynchronizedStmt() &&
                            lastStatement.asSynchronizedStmt().getBody().getStatements().getLast().isPresent() &&
                            !lastStatement.asSynchronizedStmt().getBody().getStatements().getLast().get().isReturnStmt()) {
                    Expression returnExpression = defaultValueExpr(METHOD_RETURN_TYPE, DEFAULT_VALUE);
                    ReturnStmt returnStatement = new ReturnStmt(returnExpression);
                    blockStmt.addStatement(returnStatement);
                }
            }
        }
        return blockStmt;
    }

    private static boolean isAReSweepMethodCall(MethodCallExpr mce) {
        return mce.getNameAsString().equals(RESWEEP_EXEC_CALL_NAME);
    }

    private static boolean isAnInstrumentedReturn(NodeList<Expression> args) {
        final String TYPE = String.valueOf(args.get(0));
        return TYPE.contains(RETURN_KEYWORD);
    }

    private static boolean lastStatementIsADefaultValueExpr(BlockStmt blockStmt) {
        final int BLOCK_SIZE = blockStmt.getStatements().size();
        if (BLOCK_SIZE > 0) {
            Statement lastStatement = blockStmt.getStatement(BLOCK_SIZE - 1);

            if (lastStatement.isReturnStmt()) {
                ReturnStmt lsReturn = lastStatement.asReturnStmt();
                if (lsReturn.getExpression().isPresent() &&
                        lsReturn.getExpression().get().isConditionalExpr() &&
                        lsReturn.getExpression().get().asConditionalExpr().getCondition().isMethodCallExpr()) {

                    final MethodCallExpr
                            MCE =
                            lsReturn.getExpression().get().asConditionalExpr().getCondition().asMethodCallExpr();

                    return MCE.getNameAsString().equals(RESWEEP_DEFAULT_VALUE_CALL_NAME);
                }
            }
        }
        return false;
    }

    private static boolean lastStatementIsASyncStmt(BlockStmt blockStmt) {
        final int BLOCK_SIZE = blockStmt.getStatements().size();
        if (BLOCK_SIZE > 0) {
            Statement lastStatement = blockStmt.getStatement(BLOCK_SIZE - 1);
            return lastStatement.isSynchronizedStmt();
        }
        return false;
    }

    private int generateID() {
        return classUnderTest.find(ce -> ce instanceof Stmt stmt && stmt.getDefaultSet()).size();
    }

    Statement instrument(Statement stmt) {
        if (stmt.isBlockStmt()) return instrument(stmt.asBlockStmt());
        if (stmt.isExpressionStmt()) return instrument(stmt.asExpressionStmt());
        return instrumentStmt(stmt);
    }

    private Statement instrument(ExpressionStmt exprStmt) {
        return instrumentStmt(exprStmt);
    }

    Statement instrumentDeclaration(VariableDeclarator variableDeclarator) {
        MethodCallExpr condition = generateExecMethodCallExpr(
                this.statementType, this.id, this.containingClass, operatorSet);

        if (variableDeclarator.getInitializer().isPresent()) {
            Expression oldInitializer = variableDeclarator.getInitializer().get();
            AssignExpr thenExpr = new AssignExpr(
                    new NameExpr(variableDeclarator.getName()), oldInitializer, AssignExpr.Operator.ASSIGN);

            ExpressionStmt thenStmt = new ExpressionStmt(thenExpr);
            IfStmt ifStmt = new IfStmt();
            ifStmt.setThenStmt(thenStmt);
            ifStmt.setCondition(condition);
            recordSourceFilePosition(variableDeclarator.getBegin(), variableDeclarator.getEnd());

            return ifStmt;
        }

        return new ExpressionStmt(new VariableDeclarationExpr(variableDeclarator));
    }

    AssignExpr instrument(AssignExpr assignExpr, VariableDeclarator variableDeclarator) {
        MethodCallExpr condition = generateExecMethodCallExpr(
                this.statementType, this.id, this.containingClass, operatorSet);

        Type type = variableDeclarator.getType();

        ConditionalExpr newInitializer = new ConditionalExpr(
                condition,
                new CastExpr(type, new EnclosedExpr(assignExpr.getValue())),
                defaultValueExpr(type, DEFAULT_VALUE));

        assignExpr.setValue(newInitializer);

        return assignExpr;
    }

    private void recordSourceFilePosition(Optional<Position> stmtStart, Optional<Position> stmtEnd) {
        if (stmtStart.isPresent() && stmtEnd.isPresent()) {
            Position start = stmtStart.get();
            Position end = stmtEnd.get();

            classUnderTest.setPosition(stmtDefaultTrue,
                    new SourceFilePosition(start.line, end.line, start.column, end.column));

            if (statementType == RETURN) {
                classUnderTest.setPosition(stmtDefaultFalse,
                        new SourceFilePosition(start.line, end.line, start.column, end.column));
            }
        }
    }

    private IfStmt instrumentStmt(Statement statement) {
        IfStmt instrumentedIf = new IfStmt();

        MethodCallExpr expression = generateExecMethodCallExpr(statementType, id, containingClass, operatorSet);
        instrumentedIf.setCondition(expression);
        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(statement);
        instrumentedIf.setThenStmt(blockStmt);
        recordSourceFilePosition(statement.getBegin(), statement.getEnd());
        return instrumentedIf;
    }

    public Statement instrumentInnerClassOrLambdaVarDec(Statement currentStatement,
                                                        VariableDeclarator variableDeclarator) {

        if (variableDeclarator.getInitializer().isPresent()) {
            Expression oldInitializer = variableDeclarator.getInitializer().get();
            AssignExpr thenExpr = new AssignExpr(
                    new NameExpr(variableDeclarator.getName()), oldInitializer, AssignExpr.Operator.ASSIGN);
            ExpressionStmt thenExprStmt = new ExpressionStmt(thenExpr);
            IfStmt instrumentedStatement = instrumentStmt(currentStatement);

            MethodCallExpr expression = generateExecMethodCallExpr(statementType, id, containingClass, operatorSet);
            instrumentedStatement.setCondition(expression);

            BlockStmt blockStmt = new BlockStmt();
            blockStmt.addStatement(thenExprStmt);
            instrumentedStatement.setThenStmt(blockStmt);

            return instrumentedStatement;
        }
        return currentStatement;
    }
}
