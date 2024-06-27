package org.pseudosweep.logging.formatter;

import org.pseudosweep.logging.LogLevel;

public class ThreadInfoWriter implements Formatter {

    @Override
    public String format(LogLevel logLevel, String className, String msg) {
        Thread currentThread = Thread.currentThread();
        return currentThread.getName() + ", " + currentThread.getId();
    }
}
