package org.pseudosweep.program.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.pseudosweep.program.Block;

import java.io.IOException;

public class BlockSerializer extends StdSerializer<Block> {

    public BlockSerializer() {
        this(null);
    }

    public BlockSerializer(Class<Block> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(Block value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(value.toString());
    }
}
