package org.pseudosweep.program;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.pseudosweep.program.serialize.BlockDeserializer;
import org.pseudosweep.program.serialize.BlockSerializer;
import org.pseudosweep.PseudoSweepParseException;

import java.util.Objects;

import static org.pseudosweep.util.StringUtils.*;

@JsonSerialize(using = BlockSerializer.class)
@JsonDeserialize(using = BlockDeserializer.class)
public class Block extends CoverageElement implements Comparable<Block> {

    private static final String EMPTY_SIGNIFIER = "empty", DEFAULT = "DEFAULT", NON_DEFAULT = "NON_DEFAULT";
    private final Type type;
    private boolean defaultSet;
    private boolean empty;

    public Block(@JsonProperty("type") Type type,
                 @JsonProperty("id") int id,
                 @JsonProperty("containingClass") String containingClass,
                 boolean defaultSet) {
        super(id, containingClass);
        this.type = type;
        this.empty = false;
        this.defaultSet = defaultSet;
    }

    public static Block fromString(String str) {
        String exceptionMessage = "Could not parse " + encloseQuotes(str) + " into a Block object";
        String[] fields = commaSplit(stripParentheses(stripStart(str, Block.class.getSimpleName())));
        if (fields == null || fields.length < 3) {
            throw new PseudoSweepParseException(exceptionMessage);
        }

        try {
            Type type = Type.valueOf(fields[0].trim());
            int id = Integer.parseInt(fields[1].trim());
            String containingClass = fields[2].trim();
            String defaultSetString = fields[3].trim();
            boolean empty = false;
            if (fields.length == 5) {
                empty = fields[3].trim().equals(EMPTY_SIGNIFIER);
                defaultSetString = fields[4].trim();
            }
            boolean defaultSet = defaultSetString.equals("DEFAULT");

            Block block = new Block(type, id, containingClass, defaultSet);
            block.setEmpty(empty);
            return block;
        } catch (IllegalArgumentException e) {
            throw new PseudoSweepParseException(exceptionMessage + ": " + e.getMessage());
        }
    }

    public Type getType() {
        return type;
    }

    public boolean getDefaultSet() {
        return defaultSet;
    }

    public void setDefaultSet(boolean defaultSet) {
        this.defaultSet = defaultSet;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return getId() == block.getId() &&
                getContainingClass().equals(block.getContainingClass()) &&
                getDefaultSet() == block.getDefaultSet();
    }


    public boolean equalsNoDefault(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return getId() == block.getId() &&
                getContainingClass().equals(block.getContainingClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Block.class, getId(), getContainingClass());
    }

    @Override
    public String toString() {
        return Block.class.getSimpleName() +
                encloseParentheses(commaSeparate(type, id, containingClass, (empty ? EMPTY_SIGNIFIER : ""), (defaultSet ? DEFAULT : NON_DEFAULT)));
    }

    @Override
    public int compareTo(Block other) {
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



    public enum Type {
        CONSTRUCTOR,
        METHOD,
        INITIALIZER,
    }
}
