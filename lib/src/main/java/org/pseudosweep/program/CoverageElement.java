package org.pseudosweep.program;

public abstract class CoverageElement {

    protected final int id;
    protected final String containingClass;

    public CoverageElement(int id, String containingClass) {
        this.id = id;
        this.containingClass = containingClass;
    }

    public int getId() {
        return id;
    }

    public String getContainingClass() {
        return containingClass;
    }

}
