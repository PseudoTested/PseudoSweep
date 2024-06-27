package org.pseudosweep.util;

import java.time.Duration;
import java.util.Collection;

public class StringUtils {

    public static final String OPEN_CHEVRON = "<", CLOSE_CHEVRON = ">",
            OPEN_PARENTHESIS = "(", CLOSE_PARENTHESIS = ")",
            OPEN_SQUARE_BRACKET = "[", CLOSE_SQUARE_BRACKET = "]";

    public static final String COMMA = ",", EMPTY = "", SPACE = " ", QUOTE = "\"";

    public static boolean isEmpty(String str) {
        return str.equals(EMPTY);
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || isEmpty(str);
    }

    public static String encloseQuotes(Object obj) {
        return QUOTE + obj + QUOTE;
    }

    public static String encloseParentheses(Object obj) {
        return OPEN_PARENTHESIS + obj + CLOSE_PARENTHESIS;
    }

    public static String encloseChevrons(Object obj) {
        return OPEN_CHEVRON + obj + CLOSE_CHEVRON;
    }

    public static String encloseSquareBrackets(Object obj) {
        return OPEN_SQUARE_BRACKET + obj + CLOSE_SQUARE_BRACKET;
    }

    public static String ensureEndsWith(String str, String suffix) {
        return !str.endsWith(suffix) ? str + suffix : str;
    }

    public static String join(String delimiter, Object... items) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object item : items) {
            String entry;
            if (item instanceof Object[]) {
                entry = join(delimiter, (Object[]) item);
            } else if (item instanceof Collection<?>) {
                entry = join(delimiter, ((Collection<?>) item).toArray());
            } else {
                entry = item.toString();
            }

            if (!isEmpty(entry)) {
                if (first) {
                    first = false;
                } else {
                    sb.append(delimiter);
                }
                sb.append(entry);
            }
        }
        return sb.toString();
    }

    public static String commaSeparate(Object... items) {
        return join(COMMA + SPACE, items);
    }

    public static String normalizeWhiteSpace(String str) {
        return str.trim().replaceAll("\\s+", " ");
    }

    public static String durationAsHMSMString(Duration duration) {
        return String.format("%d:%02d:%02d:%03d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart());
    }

    public static String millisAsHMSMString(long millis) {
        return durationAsHMSMString(Duration.ofMillis(millis));
    }

    public static String threadInfoString(Thread thread) {
        return "thread " + encloseQuotes(thread.getName()) + " " + encloseParentheses("ID: " + thread.getId());
    }

    public static String stripStart(String str, String start) {
        if (str == null) {
            return null;
        }
        return str.startsWith(start) ? str.substring(start.length()) : null;
    }

    public static String stripEnd(String str, String end) {
        if (str == null) {
            return null;
        }
        return str.endsWith(end) ? str.substring(0, str.length() - end.length()) : null;
    }

    public static String stripStartAndEnd(String str, String start, String end) {
        return stripEnd(stripStart(str, start), end);
    }

    public static String stripParentheses(String str) {
        return stripStartAndEnd(str, OPEN_PARENTHESIS, CLOSE_PARENTHESIS);
    }

    public static String[] commaSplit(String str) {
        if (str == null) {
            return null;
        }
        return str.split(COMMA);
    }

}
