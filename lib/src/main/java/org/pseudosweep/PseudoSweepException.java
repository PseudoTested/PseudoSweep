package org.pseudosweep;


public class PseudoSweepException extends RuntimeException{

    public PseudoSweepException(Throwable t) {
        super(t);
    }

    public PseudoSweepException(String msg) {
        super(msg);
    }
}
