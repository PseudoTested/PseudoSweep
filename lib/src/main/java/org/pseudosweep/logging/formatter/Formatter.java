package org.pseudosweep.logging.formatter;

import org.pseudosweep.logging.LogLevel;

public interface Formatter {

    public abstract String format(LogLevel logLevel, String className, String msg);

}
