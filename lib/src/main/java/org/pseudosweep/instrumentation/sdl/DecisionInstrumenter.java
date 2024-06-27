package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.Position;
import com.github.javaparser.ast.expr.*;
import org.pseudosweep.program.ClassUnderTest;
import org.pseudosweep.program.Decision;
import org.pseudosweep.program.SourceFilePosition;

public class DecisionInstrumenter {
    private final ClassUnderTest classUnderTest;
    private final int decisionId;

    private final Decision trueDecision;
    private final Decision.Type decisionType;

    DecisionInstrumenter(Decision.Type decisionType, ClassUnderTest classUnderTest) {
        this.classUnderTest = classUnderTest;
        this.decisionId = generateID();
        this.decisionType = decisionType;

        trueDecision = new Decision(true, decisionType, decisionId, classUnderTest.getFullClassName());
        classUnderTest.addCoverageElement(trueDecision);
    }

    private int generateID() {
        return classUnderTest.find(ce -> ce instanceof Decision decision && decision.getTruthValue()).size();
    }


    Expression instrument(Expression expression) {
        SourceFilePosition sourceFilePosition = getSourceFilePosition(expression);
        classUnderTest.setPosition(trueDecision, sourceFilePosition);

        return InstrumentationCallGenerator.generateExpr(expression, decisionType, decisionId, classUnderTest.getFullClassName());
    }

    static SourceFilePosition getSourceFilePosition(Expression expression) {
        if (expression.getBegin().isPresent() && expression.getEnd().isPresent()) {
            Position start = expression.getBegin().get();
            Position end = expression.getEnd().get();
            return new SourceFilePosition(start.line, end.line, start.column, end.column);
        }
        return null;
    }

}
