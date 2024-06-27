package org.pseudosweep.logging.formatter;

import org.pseudosweep.logging.LogLevel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeWriter implements Formatter {

    private final DateTimeFormatter dateTimeFormatter;

    public DateTimeWriter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @Override
    public String format(LogLevel logLevel, String className, String msg) {
        return LocalDateTime.now().format(dateTimeFormatter);
    }
}
