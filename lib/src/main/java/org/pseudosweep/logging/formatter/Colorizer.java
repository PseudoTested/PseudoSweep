package org.pseudosweep.logging.formatter;

import org.pseudosweep.logging.LogLevel;

public class Colorizer implements Formatter {

    private static final String RESET = "\033[0m";

    private static final String GREY = "\u001B[90m",
                                RED = "\u001B[31m",
                                YELLOW = "\u001B[33m",
                                WHITE = "\u001B[37m";

    private final Formatter subformatter;

    public Colorizer(Formatter subformatter) {
        this.subformatter = subformatter;
    }

    @Override
    public String format(LogLevel logLevel, String className, String msg) {
        String string = subformatter.format(logLevel, className, msg);
        return switch (logLevel) {
            case FATAL, ERROR -> RED + string + RESET;
            case WARN -> YELLOW + string + RESET;
            case DEBUG, TRACE -> GREY + string + RESET;
            default -> WHITE + string + RESET;
        };
    }

}
