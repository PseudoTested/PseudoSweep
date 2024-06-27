package org.pseudosweep.logging.formatter;

import org.pseudosweep.logging.LogLevel;

import java.util.Arrays;

import static org.pseudosweep.util.StringUtils.join;

public class Separator implements Formatter {

    private String separator;
    private Formatter[] subformatters;

    public Separator(String separator, Formatter... subformatters) {
        this.separator = separator;
        this.subformatters = subformatters;
    }

    @Override
    public String format(LogLevel logLevel, String className, String msg) {
        return join(separator, Arrays.stream(subformatters).map(s -> s.format(logLevel, className, msg)).toList());
    }
}
