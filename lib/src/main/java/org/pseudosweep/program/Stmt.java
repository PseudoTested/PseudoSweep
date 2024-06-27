package org.pseudosweep.program;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.pseudosweep.program.serialize.StmtDeserializer;
import org.pseudosweep.program.serialize.StmtSerializer;
import org.pseudosweep.PseudoSweepParseException;

import java.util.Objects;

import static org.pseudosweep.util.StringUtils.*;

@JsonSerialize(using = StmtSerializer.class)
@JsonDeserialize(using = StmtDeserializer.class)
public class Stmt extends CoverageElement implements Comparable<Stmt> {

    public static final String DEFAULT = "DEFAULT", NON_DEFAULT = "NON_DEFAULT";

    public enum Type {
        BREAK,
        CONTINUE,
        DO,
        EXPRESSION,
        FOR,
        FOR_EACH,
        IF,
        INNER_CLASS,
        INNER_CLASS_RETURN,
        LAMBDA,
        LAMBDA_RETURN,
        RETURN,
        SWITCH,
        SWITCH_ENTRY_ASSIGNMENT,
        THROW,
        TRY,
        VARIABLE_DECLARATION,
        WHILE

    }

    private final Type type;
    private final boolean defaultSet;

    public Stmt(@JsonProperty("type") Type type,
                @JsonProperty("id") int id,
                @JsonProperty("containingClass") String containingClass,
                boolean defaultSet) {
        super(id, containingClass);
        this.type = type;
        this.defaultSet = defaultSet;
    }

    public Type getType() {
        return type;
    }

    public boolean getDefaultSet() {
        return defaultSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stmt stmt = (Stmt) o;
        return getId() == stmt.getId() &&
                getContainingClass().equals(stmt.getContainingClass()) &&
                getDefaultSet() == stmt.getDefaultSet();
    }

    public boolean equalsNoDefault(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stmt stmt = (Stmt) o;
        return getId() == stmt.getId() &&
                getContainingClass().equals(stmt.getContainingClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Stmt.class, getId(), getContainingClass());
    }


    @Override
    public String toString() {
        return Stmt.class.getSimpleName() +
                encloseParentheses(commaSeparate(type, id, containingClass, (defaultSet ? DEFAULT : NON_DEFAULT)));
    }


    public static Stmt fromString(String str) {
        String exceptionMessage = "Could not parse " + encloseQuotes(str) + " into a Stmt object";
        String[] fields = commaSplit(stripParentheses(stripStart(str, Stmt.class.getSimpleName())));
        if (fields == null || fields.length < 3) {
            throw new PseudoSweepParseException(exceptionMessage);
        }
        try {
            Stmt.Type type = Stmt.Type.valueOf(fields[0].trim());
            int id = Integer.parseInt(fields[1].trim());
            String containingClass = fields[2].trim();
            String defaultSetString = fields[3].trim();
            boolean defaultSet = defaultSetString.equals("DEFAULT");
            return new Stmt(type, id, containingClass, defaultSet);
        } catch (IllegalArgumentException e) {
            throw new PseudoSweepParseException(exceptionMessage + ": " + e.getMessage());
        }

    }

    @Override
    public int compareTo(Stmt other) {
        if (this.getContainingClass().equals(other.getContainingClass())) {
            if (this.getId() == other.getId()) {
                if (this.getDefaultSet() == other.getDefaultSet()) {
                    return 0;
                } else if (this.getDefaultSet()) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return this.getId() - other.getId();
            }
        } else {
            return this.getContainingClass().compareTo(other.getContainingClass());
        }
    }
}
