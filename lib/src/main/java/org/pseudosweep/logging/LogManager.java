package org.pseudosweep.logging;

import java.util.HashMap;
import java.util.Map;

public class LogManager {

    private final static Map<String, Appender> appenders = new HashMap<>();

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz.getCanonicalName());
    }

    public static void log(LogLevel logLevel, String className, String msg) {
        appenders.values().forEach(appender -> appender.log(logLevel, className, msg));
    }

    public static void addAppender(String name, Appender appender) {
        appenders.put(name, appender);
    }

    public static void removeAppender(String name) {
        appenders.remove(name);
    }
}
