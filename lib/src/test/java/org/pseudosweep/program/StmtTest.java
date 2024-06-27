package org.pseudosweep.program;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class StmtTest {
    @Test
    void type() {
        assertThat(Stmt.Type.valueOf("EXPRESSION"), equalTo(Stmt.Type.EXPRESSION));
    }

    @Test
    public void serializationAndDeserialization() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Stmt.Type type = Stmt.Type.EXPRESSION;
        int id = 1;
        String containingClass = "com.test.Test";

        Stmt out = new Stmt(type, id, containingClass, true);

        // serialize
        String json = objectMapper.writeValueAsString(out);

        // deserialize
        Stmt in = objectMapper.readValue(json, Stmt.class);

        assertThat(in.getType(), equalTo(out.getType()));
        assertThat(in.getId(), equalTo(out.getId()));
        assertThat(in.getContainingClass(), equalTo(out.getContainingClass()));
    }

    @Test
    void testToString() {
        String expected = "Stmt(EXPRESSION, 0, examples.triangle.Triangle, DEFAULT)";
        Stmt stmt = new Stmt(Stmt.Type.EXPRESSION, 0, "examples.triangle.Triangle", true);

        assertEquals(expected, stmt.toString());
    }

    @Test
    void fromString() {
        String input = "Stmt(EXPRESSION, 0, examples.triangle.Triangle, DEFAULT)";
        Stmt expected = new Stmt(Stmt.Type.EXPRESSION, 0, "examples.triangle.Triangle", true);
        Stmt result = Stmt.fromString(input);
        assertThat(expected, equalTo(result));
    }

    @Test
    void equality() {
        Stmt stmt1 = new Stmt(Stmt.Type.EXPRESSION, 10, "com.test.Test", true);
        Stmt stmt2 = new Stmt(Stmt.Type.EXPRESSION, 10, "com.test.Test", true);

        assertThat(stmt1, equalTo(stmt2));
    }

    @Test
    void nonEquality() {
        Stmt stmt1 = new Stmt(Stmt.Type.EXPRESSION, 10, "com.test.Test", true);
        Stmt stmt2 = new Stmt(Stmt.Type.VARIABLE_DECLARATION, 11, "com.test.Test", true);

        assertThat(stmt1, not(equalTo(stmt2)));
    }
}
