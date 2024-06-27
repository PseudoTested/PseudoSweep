package org.pseudosweep.program;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.pseudosweep.program.serialize.DecisionDeserializer;
import org.pseudosweep.program.serialize.DecisionSerializer;
import org.pseudosweep.PseudoSweepParseException;

import java.util.Objects;

import static org.pseudosweep.util.StringUtils.*;

@JsonSerialize(using = DecisionSerializer.class)
@JsonDeserialize(using = DecisionDeserializer.class)
public class Decision extends BooleanExpression implements Comparable<Decision> {

    public enum Type {
        IF,
        LOOP,
        TERNARY
    }

    private final Type type;

    public Decision(@JsonProperty("truthValue") boolean truthValue,
                    @JsonProperty("type") Type type,
                    @JsonProperty("id") int id,
                    @JsonProperty("containingClass") String containingClass) {
        super(truthValue, id, containingClass);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Decision decision = (Decision) o;
        return getTruthValue() == decision.getTruthValue() &&
                getId() == decision.getId() &&
                getContainingClass().equals(decision.getContainingClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Decision.class, getTruthValue(), getId(), getContainingClass());
    }

    @Override
    public String toString() {
        return Decision.class.getSimpleName() + encloseParentheses(commaSeparate(truthValue, type, id, containingClass));
    }

    public static Decision fromString(String str) {
        String exceptionMessage = "Could not parse " + encloseQuotes(str) + " into a Decision object";
        String[] fields = commaSplit(stripParentheses(stripStart(str, Decision.class.getSimpleName())));
        if (fields == null || fields.length != 4) {
            throw new PseudoSweepParseException(exceptionMessage);
        }

        try {
            boolean truthValue = Boolean.parseBoolean(fields[0]);
            Decision.Type type = Decision.Type.valueOf(fields[1].trim());
            int id = Integer.parseInt(fields[2].trim());
            String containingClass = fields[3].trim();
            return new Decision(truthValue, type, id, containingClass);
        } catch (IllegalArgumentException e) {
            throw new PseudoSweepParseException(exceptionMessage + ": " + e.getMessage());
        }
    }

    @Override
    public int compareTo(Decision other) {
        if (this.getContainingClass().equals(other.getContainingClass())) {
            if (this.getId() == other.getId()) {
                if (this.getTruthValue() == other.getTruthValue()) {
                    return 0;
                } else if (this.getTruthValue()) {
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
