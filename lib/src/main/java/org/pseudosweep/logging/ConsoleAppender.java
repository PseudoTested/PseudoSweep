package org.pseudosweep.logging;

import org.pseudosweep.logging.formatter.Formatter;

public class ConsoleAppender extends Appender {

    public ConsoleAppender(LogLevel logLevel, Formatter formatter) {
        super(logLevel, formatter);
    }

    @Override
    protected void log(String string) {
        System.out.println(string);
    }
}
