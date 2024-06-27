package org.pseudosweep.logging;

import org.pseudosweep.logging.formatter.Formatter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileAppender extends Appender {

    private final PrintWriter out;

    public FileAppender(String fileName, LogLevel logLevel, Formatter formatter) throws IOException {
        super(logLevel, formatter);
        out = new PrintWriter(new FileWriter(fileName));
    }

    @Override
    protected void log(String string) {
        out.println(string);
        out.flush();
    }
}
