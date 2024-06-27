package org.pseudosweep.program.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.pseudosweep.program.Decision;

import java.io.IOException;

public class DecisionDeserializer extends StdDeserializer<Decision> {

    public DecisionDeserializer() {
        this(null);
    }

    public DecisionDeserializer(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public Decision deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return Decision.fromString(jsonParser.getValueAsString());
    }
}
