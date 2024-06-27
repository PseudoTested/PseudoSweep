package org.pseudosweep.logging;

public enum LogLevel {
    OFF,
    FATAL,
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE,
    ALL;

    public boolean higherOrSame(LogLevel other) {
        return this.compareTo(other) >= 0;
    }
}
