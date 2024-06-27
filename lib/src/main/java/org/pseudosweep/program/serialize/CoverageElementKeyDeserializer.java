package org.pseudosweep.program.serialize;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import org.pseudosweep.program.Block;
import org.pseudosweep.program.CoverageElement;
import org.pseudosweep.program.Decision;
import org.pseudosweep.program.Stmt;
import org.pseudosweep.PseudoSweepParseException;

import static org.pseudosweep.util.StringUtils.encloseQuotes;

public class CoverageElementKeyDeserializer extends KeyDeserializer {
    @Override
    public CoverageElement deserializeKey(String key, final DeserializationContext deserializationContext) {
        try {
            return Decision.fromString(key);
        } catch (PseudoSweepParseException e1) {
            try {
                return Stmt.fromString(key);
            } catch (PseudoSweepParseException e2) {
                try {
                    return Block.fromString(key);
                } catch (PseudoSweepParseException e3) {
                    throw new PseudoSweepParseException("Cannot parse CoverageElement from " + encloseQuotes(key));
                }
            }
        }
    }
}


