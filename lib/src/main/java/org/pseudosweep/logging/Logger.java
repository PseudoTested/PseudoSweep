package org.pseudosweep.logging;

import java.util.Arrays;

public class Logger {

    String className;

    public Logger(String className) {
        this.className = className;
    }

    public void fatal(String msg) {
        LogManager.log(LogLevel.FATAL, className, msg);
    }

    public void fatal(Throwable t) {
        fatal(t.getClass().getCanonicalName() + " thrown: " + t.getMessage());
        Arrays.stream(t.getStackTrace()).forEach(ste -> fatal("\t at " + ste));
    }

    public void error(String msg) {
        LogManager.log(LogLevel.ERROR, className, msg);
    }

    public void warn(String msg) {
        LogManager.log(LogLevel.WARN, className, msg);
    }

    public void info(String msg) {
        LogManager.log(LogLevel.INFO, className, msg);
    }

    public void debug(String msg) {
        LogManager.log(LogLevel.DEBUG, className, msg);
    }

    public void trace(String msg) {
        LogManager.log(LogLevel.TRACE, className, msg);
    }
}
