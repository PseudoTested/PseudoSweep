package org.pseudosweep.testframework.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.pseudosweep.testframework.TestOutcome;

import java.io.IOException;

public class TestOutcomeDeserializer extends StdDeserializer<TestOutcome> {

    public TestOutcomeDeserializer() {
        this(null);
    }

    public TestOutcomeDeserializer(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public TestOutcome deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return TestOutcome.fromString(jsonParser.getValueAsString());
    }
}
