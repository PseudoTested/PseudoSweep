package org.pseudosweep.program.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.pseudosweep.program.Stmt;

import java.io.IOException;

public class StmtDeserializer extends StdDeserializer<Stmt> {

    public StmtDeserializer() {
        this(null);
    }

    public StmtDeserializer(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public Stmt deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return Stmt.fromString(jsonParser.getValueAsString());
    }
}
