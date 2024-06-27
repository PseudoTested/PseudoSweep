package org.pseudosweep.testframework;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.pseudosweep.PseudoSweepParseException;
import org.pseudosweep.testframework.serialize.TestOutcomeDeserializer;
import org.pseudosweep.testframework.serialize.TestOutcomeSerializer;

import java.util.Objects;

import static org.pseudosweep.util.StringUtils.*;

@JsonSerialize(using = TestOutcomeSerializer.class)
@JsonDeserialize(using = TestOutcomeDeserializer.class)
public class TestOutcome {

    public enum Type {
        PASSED,
        FAILED,
        SKIPPED,
        TIMED_OUT,
        THREW_EXCEPTION
    }

    private final Type type;
    private final long runTime;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String exceptionInfo;

    public TestOutcome(@JsonProperty("type") Type type,
                       @JsonProperty("runtime") long runtime) {
        this.type = type;
        this.runTime = runtime;
    }

    public Type getType() {
        return type;
    }

    public long getRunTime() {
        return runTime;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    public void setExceptionInfoFromException(Throwable t) {
        /*
            Sometimes the stack trace isn't available due to JVM optimizations.
            See https://stackoverflow.com/questions/2411487/nullpointerexception-in-java-with-no-stacktrace
            To always get the stack traces use the parameter -XX:-OmitStackTraceInFastThrow
         */
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        String stackTraceSummary =  (stackTraceElements.length > 0)
                ? stackTraceElements[0].toString()
                : "Stack trace unavailable. Re-run with -XX:-OmitStackTraceInFastThrow";
        exceptionInfo = t + " " + encloseChevrons(stackTraceSummary);
    }

    public boolean passed() {
        return type == Type.PASSED;
    }

    public String toString() {
        return type + encloseParentheses(runTime + (exceptionInfo == null ? "" : ", " + exceptionInfo));
    }

    public static TestOutcome fromString(String str) throws PseudoSweepParseException {
        String exceptionMessage = "Could not parse " + encloseQuotes(str) + " into a TestOutcome object";

        int openParenthesisPos = str.indexOf(OPEN_PARENTHESIS);
        if (openParenthesisPos == -1) {
            throw new PseudoSweepParseException(exceptionMessage);
        }

        try {
            Type type = Type.valueOf(str.substring(0, openParenthesisPos));

            String fields = stripParentheses(str.substring(openParenthesisPos));
            String timeOutStr = fields;
            String exceptionInfo = null;
            int commaPos = fields.indexOf(COMMA);
            if (commaPos != -1) {
                timeOutStr = fields.substring(0, commaPos);
                exceptionInfo = fields.substring(commaPos+1).trim();
            }

            int runtime = Integer.parseInt(timeOutStr);
            TestOutcome testOutcome = new TestOutcome(type, runtime);
            if (exceptionInfo != null) {
                testOutcome.setExceptionInfo(exceptionInfo);
            }
            return testOutcome;
        } catch (IllegalArgumentException e) {
            throw new PseudoSweepParseException(exceptionMessage + ": " + e.getMessage());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestOutcome that = (TestOutcome) o;
        return getRunTime() == that.getRunTime() && getType() == that.getType() && Objects.equals(getExceptionInfo(), that.getExceptionInfo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getRunTime(), getExceptionInfo());
    }
}

