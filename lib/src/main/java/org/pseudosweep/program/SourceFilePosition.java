package org.pseudosweep.program;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


public class SourceFilePosition {

    final int startLine, endLine, startCol, endCol;

    public SourceFilePosition(@JsonProperty("startLine") int startLine,
                              @JsonProperty("endLine") int endLine,
                              @JsonProperty("startCol") int startCol,
                              @JsonProperty("endCol") int endCol) {
        this.startLine = startLine;
        this.endLine = endLine;
        this.startCol = startCol;
        this.endCol = endCol;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getStartCol() {
        return startCol;
    }

    public int getEndCol() {
        return endCol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceFilePosition that = (SourceFilePosition) o;
        return getStartLine() == that.getStartLine() && getEndLine() == that.getEndLine() && getStartCol() == that.getStartCol() && getEndCol() == that.getEndCol();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStartLine(), getEndLine(), getStartCol(), getEndCol());
    }

    @Override
    public String toString() {
        return startLine + ":" + startCol + " - " + endLine + ":" + endCol;
    }
}
