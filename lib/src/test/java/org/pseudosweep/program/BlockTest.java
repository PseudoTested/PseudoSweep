package org.pseudosweep.program;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

public class BlockTest {

    final boolean DEFAULT_VALUE = true;

    @Test
    public void type() {
        assertThat(Block.Type.valueOf("METHOD"), equalTo(Block.Type.METHOD));
    }

    @Test
    public void serializationAndDeserialization() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Block.Type type = Block.Type.METHOD;
        int id = 1;
        String containingClass = "com.test.Test";

        Block out = new Block(type, id, containingClass,DEFAULT_VALUE);

        // serialize
        String json = objectMapper.writeValueAsString(out);

        // deserialize
        Block in = objectMapper.readValue(json, Block.class);

        assertThat(in.getType(), equalTo(out.getType()));
        assertThat(in.getId(), equalTo(out.getId()));
        assertThat(in.getContainingClass(), equalTo(out.getContainingClass()));
    }

    @Test
    public void toAndFromString() {
        Block b1 = new Block(Block.Type.METHOD, 1, "com.test.Test", DEFAULT_VALUE);
        String str = b1.toString();
        assertThat(str, equalTo("Block(METHOD, 1, com.test.Test, DEFAULT)"));
        Block b2 = Block.fromString(str);
        assertThat(b2, equalTo(b1));
    }

    @Test
    public void toAndFromString_empty() {
        Block b1 = new Block(Block.Type.METHOD, 1, "com.test.Test", DEFAULT_VALUE);
        b1.setEmpty(true);
        String str = b1.toString();
        assertThat(str, equalTo("Block(METHOD, 1, com.test.Test, empty, DEFAULT)"));
        Block b2 = Block.fromString(str);
        assertThat(b2, equalTo(b1));
    }

    @Test
    public void equality() {
        Block block1 = new Block(Block.Type.CONSTRUCTOR, 10, "com.test.Test", DEFAULT_VALUE);
        Block block2 = new Block(Block.Type.CONSTRUCTOR, 10, "com.test.Test", DEFAULT_VALUE);

        assertThat(block1, equalTo(block2));
    }

    @Test
    void nonEquality() {
        Block block1 = new Block(Block.Type.CONSTRUCTOR, 10, "com.test.Test", DEFAULT_VALUE);
        Block block2 = new Block(Block.Type.METHOD, 10, "com.test.Test", DEFAULT_VALUE);
        Block block3 = new Block(Block.Type.CONSTRUCTOR, 11, "com.test.Test", DEFAULT_VALUE);
        Block block4 = new Block(Block.Type.CONSTRUCTOR, 10, "com.test.Testing", DEFAULT_VALUE);
        Block block5 = new Block(Block.Type.CONSTRUCTOR, 10, "com.test.Testing", !DEFAULT_VALUE);

        assertThat(block1, equalTo(block2));
        assertThat(block1, not(equalTo(block3)));
        assertThat(block1, not(equalTo(block4)));
        assertThat(block1, not(equalTo(block5)));
    }

    @Test
    void compare() {
        Block block1 = new Block(Block.Type.CONSTRUCTOR, 10, "com.test.Test", DEFAULT_VALUE);
        Block block2 = new Block(Block.Type.METHOD, 11, "com.test.Test", DEFAULT_VALUE);
        Block block3 = new Block(Block.Type.CONSTRUCTOR, 12, "com.test.Test", DEFAULT_VALUE);
        Block block4 = new Block(Block.Type.CONSTRUCTOR, 10, "com.test.Testing", DEFAULT_VALUE);

        List<Block> blocks = new ArrayList<>();
        blocks.add(block4);
        blocks.add(block1);
        blocks.add(block3);
        blocks.add(block2);
        Collections.sort(blocks);

        // containingClass first, then id (type is insignificant)

        assertThat(blocks.get(0), equalTo(block1));
        assertThat(blocks.get(1), equalTo(block2));
        assertThat(blocks.get(2), equalTo(block3));
        assertThat(blocks.get(3), equalTo(block4));
    }
}
