package org.pseudosweep.testframework.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.pseudosweep.testframework.TestOutcome;

import java.io.IOException;

public class TestOutcomeSerializer extends StdSerializer<TestOutcome> {

    public TestOutcomeSerializer() {
        this(null);
    }

    public TestOutcomeSerializer(Class<TestOutcome> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(TestOutcome value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(value.toString());
    }
}
