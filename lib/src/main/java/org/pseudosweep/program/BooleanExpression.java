package org.pseudosweep.program;

public abstract class BooleanExpression extends CoverageElement {

    protected final boolean truthValue;

    public BooleanExpression(boolean truthValue, int id, String containingClass) {
        super(id, containingClass);
        this.truthValue = truthValue;
    }

    public boolean getTruthValue() {
        return truthValue;
    }
}
