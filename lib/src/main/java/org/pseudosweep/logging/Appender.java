package org.pseudosweep.logging;

import org.pseudosweep.logging.formatter.Formatter;

public abstract class Appender {

    protected LogLevel logLevel;
    protected Formatter formatter;

    public Appender(LogLevel logLevel, Formatter formatter) {
        this.logLevel = logLevel;
        this.formatter = formatter;
    }

    public void log(LogLevel logLevel, String className, String msg) {
        if (this.logLevel.higherOrSame(logLevel)) {
            log(formatter.format(logLevel, className, msg));
        }
    }

    protected abstract void log(String str);
}
