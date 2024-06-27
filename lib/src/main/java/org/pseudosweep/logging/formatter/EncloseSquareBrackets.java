package org.pseudosweep.logging.formatter;

import org.pseudosweep.logging.LogLevel;

import static org.pseudosweep.util.StringUtils.encloseSquareBrackets;

public class EncloseSquareBrackets implements Formatter {

    private final Formatter subformatter;

    public EncloseSquareBrackets(Formatter subformatter) {
        this.subformatter = subformatter;
    }

    @Override
    public String format(LogLevel logLevel, String className, String msg) {
        return encloseSquareBrackets(subformatter.format(logLevel, className, msg));
    }
}
