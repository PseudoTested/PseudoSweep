package org.pseudosweep.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class StringUtilsTest {

    @Test
    public void whiteSpaceCompressed() {
        String str =
                """      
                    hello
                              
                         phil
                """;

        assertThat(StringUtils.normalizeWhiteSpace(str), equalTo("hello phil"));
    }

    @Test
    public void stripEnd() {
        assertThat(StringUtils.stripEnd("cheerlessly", "lessly"), equalTo("cheer"));
        assertThat(StringUtils.stripEnd("cheerful", "less"), equalTo(null));
    }

    @Test
    public void stripParentheses() {
        assertThat(StringUtils.stripParentheses("(go ahead)"), equalTo("go ahead"));
        assertThat(StringUtils.stripParentheses("<go ahead>"), equalTo(null));
    }

    @Test
    public void commaSplit() {
        String[] elements = StringUtils.commaSplit("hello, phil, mcminn");
        assertThat(elements.length, equalTo(3));
    }
}
