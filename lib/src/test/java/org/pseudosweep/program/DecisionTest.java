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

public class DecisionTest {

    @Test
    public void serializationAndDeserialization() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        boolean truthValue = true;
        Decision.Type type = Decision.Type.IF;
        int id = 1;
        String containingClass = "com.test.Test";

        Decision out = new Decision(truthValue, type, id, containingClass);

        // serialize
        String json = objectMapper.writeValueAsString(out);

        // deserialize
        Decision in = objectMapper.readValue(json, Decision.class);

        assertThat(in.getTruthValue(), equalTo(out.getTruthValue()));
        assertThat(in.getType(), equalTo(out.getType()));
        assertThat(in.getId(), equalTo(out.getId()));
        assertThat(in.getContainingClass(), equalTo(out.getContainingClass()));
    }

    @Test
    public void toAndFromString() {
        Decision d1 = new Decision(true, Decision.Type.LOOP, 2, "com.test.Test");
        String str = d1.toString();
        assertThat(str, equalTo("Decision(true, LOOP, 2, com.test.Test)"));
        Decision d2 = Decision.fromString(str);
        assertThat(d2, equalTo(d1));
    }

    @Test
    public void equality() {
        Decision d1 = new Decision(true, Decision.Type.IF, 1, "com.test.Test");
        Decision d2 = new Decision(true, Decision.Type.IF, 1, "com.test.Test");

        assertThat(d1, equalTo(d2));
    }

    @Test
    public void nonEquality() {
        Decision d1 = new Decision(true, Decision.Type.IF, 1, "com.test.Test");
        Decision d2 = new Decision(false, Decision.Type.IF, 1, "com.test.Test");
        Decision d3 = new Decision(false, Decision.Type.LOOP, 1, "com.test.Test");
        Decision d4 = new Decision(false, Decision.Type.IF, 2, "com.test.Test");
        Decision d5 = new Decision(false, Decision.Type.IF, 2, "com.test.Best");

        assertThat(d1, not(equalTo(d2)));
        assertThat(d1, not(equalTo(d3)));
        assertThat(d1, not(equalTo(d4)));
        assertThat(d1, not(equalTo(d5)));
    }

    @Test void compare() {
        Decision d1 = new Decision(true, Decision.Type.IF, 1, "com.test.Test");
        Decision d2 = new Decision(false, Decision.Type.IF, 2, "com.test.Test");
        Decision d3 = new Decision(false, Decision.Type.LOOP, 1, "com.test.Test");
        Decision d4 = new Decision(false, Decision.Type.IF, 2, "com.test.Best");

        List<Decision> decisions = new ArrayList<>();
        decisions.add(d2);
        decisions.add(d3);
        decisions.add(d1);
        decisions.add(d4);
        Collections.sort(decisions);

        // containingClass first, then id, then truthValue (type is insignificant)
        assertThat(decisions.get(0), equalTo(d4));
        assertThat(decisions.get(1), equalTo(d1));
        assertThat(decisions.get(2), equalTo(d3));
        assertThat(decisions.get(3), equalTo(d2));
    }
}
