package org.pseudosweep.logging.formatter;

import org.pseudosweep.logging.LogLevel;

public class MessageWriter implements Formatter {

    @Override
    public String format(LogLevel logLevel, String className, String msg) {
        return msg;
    }
}
