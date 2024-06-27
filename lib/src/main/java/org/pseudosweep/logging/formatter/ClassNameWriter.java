package org.pseudosweep.logging.formatter;

import org.pseudosweep.logging.LogLevel;

public class ClassNameWriter implements Formatter {

    @Override
    public String format(LogLevel logLevel, String className, String msg) {
        return className;
    }
}
