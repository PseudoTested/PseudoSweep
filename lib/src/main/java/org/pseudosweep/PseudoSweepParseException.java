package org.pseudosweep;

public class PseudoSweepParseException extends PseudoSweepException {

    public PseudoSweepParseException(Exception e) {
        super(e);
    }

    public PseudoSweepParseException(String msg) {
        super(msg);
    }
}
