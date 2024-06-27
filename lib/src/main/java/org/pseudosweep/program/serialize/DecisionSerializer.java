package org.pseudosweep.program.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.pseudosweep.program.Decision;

import java.io.IOException;

public class DecisionSerializer extends StdSerializer<Decision> {

    public DecisionSerializer() {
        this(null);
    }

    public DecisionSerializer(Class<Decision> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(Decision value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(value.toString());
    }
}
