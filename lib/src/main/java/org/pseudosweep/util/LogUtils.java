package org.pseudosweep.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogUtils {

    public static final String
            EVAL_LABEL = "eval:",
            EXEC_LABEL = "exec:",
            ID_LABEL = "id:",
            PIPE = "|",
            IF_THEN_START = "if_then_start",
            IF_THEN_END = "if_then_end",
            IF_ELSE_START = "if_else_start",
            IF_ELSE_END = "if_else_end",
            FOR_START = "for_start",
            TRY_START = "try_start",
            TRY_END = "try_end",
            CATCH_START = "catch_start",
            CATCH_END = "catch_end",
            FOR_INCREMENT = "for_increment",
            FOR_END = "for_end",
            INNER_STATEMENT = "single_statement_executed",
            WHILE_START = "while_start",
            WHILE_END = "while_end",
            DO_WHILE_START = "do_while_start",
            DO_WHILE_END = "do_while_end",
            RETURN_START = "return_start",
            RETURN_ACTUAL = "return_actual",
            RETURN_DEFAULT = "return-default",
            METHOD_START = "method_start",
            METHOD_END = "method_end";


    public static List<String> readLog(String logfileName) throws IOException {

        Path path = Path.of(logfileName);

        List<String> result;
        try (Stream<String> lines = Files.lines(path)) {
            result = lines.collect(Collectors.toList());
        }

        List<String> logs = new ArrayList<>();
        result.forEach(str -> logs.add(str.strip()));
        return logs;

    }


}
