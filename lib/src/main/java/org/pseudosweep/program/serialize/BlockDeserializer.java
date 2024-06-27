package org.pseudosweep.program.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.pseudosweep.program.Block;

import java.io.IOException;

public class BlockDeserializer extends StdDeserializer<Block> {

    public BlockDeserializer() {
        this(null);
    }

    public BlockDeserializer(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public Block deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return Block.fromString(jsonParser.getValueAsString());
    }
}
