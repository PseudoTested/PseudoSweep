package org.pseudosweep.testframework;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringStartsWith.startsWith;

public class TestOutcomeTest {

    @Test
    public void serializationAndDeserialization() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        TestOutcome out = new TestOutcome(TestOutcome.Type.PASSED, 100);

        // serialize
        String json = objectMapper.writeValueAsString(out);

        // deserialize
        TestOutcome in = objectMapper.readValue(json, TestOutcome.class);
        assertThat(in.getType(), equalTo(out.getType()));
        assertThat(in.getRunTime(), equalTo(out.getRunTime()));
        assertThat(in.getExceptionInfo(), equalTo(null));

        // test additional property
        out.setExceptionInfoFromException(new RuntimeException());
        json = objectMapper.writeValueAsString(out);
        in = objectMapper.readValue(json, TestOutcome.class);
        assertThat(in.getExceptionInfo(), startsWith("java.lang.RuntimeException"));
    }

    @Test
    public void toAndFromString() {
        TestOutcome to1 = new TestOutcome(TestOutcome.Type.PASSED, 200);
        to1.setExceptionInfo("java.bibblebobble.WobbleException, caused by a cosmic ray");
        String str = to1.toString();
        assertThat(str, equalTo("PASSED(200, java.bibblebobble.WobbleException, caused by a cosmic ray)"));
        TestOutcome to2 = TestOutcome.fromString(str);
        assertThat(to2, equalTo(to1));
    }
}
