package org.pseudosweep.program.serialize;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.pseudosweep.program.Stmt;

import java.io.IOException;

public class StmtSerializer extends StdSerializer<Stmt> {

    public StmtSerializer() {
        this(null);
    }

    public StmtSerializer(Class<Stmt> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(Stmt value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(value.toString());
    }
}

