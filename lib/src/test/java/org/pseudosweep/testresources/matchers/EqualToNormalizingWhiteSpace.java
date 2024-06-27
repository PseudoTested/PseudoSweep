package org.pseudosweep.testresources.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static org.pseudosweep.util.StringUtils.encloseQuotes;
import static org.pseudosweep.util.StringUtils.normalizeWhiteSpace;

public class EqualToNormalizingWhiteSpace extends TypeSafeDiagnosingMatcher<String> {

    private final String expectedOriginal, expected;

    public EqualToNormalizingWhiteSpace(String expected) {
        this.expectedOriginal = expected;
        this.expected = normalizeWhiteSpace(expected);
    }

    @Override
    protected boolean matchesSafely(String actual, Description description) {
        description.appendText("was " + encloseQuotes(actual));
        return normalizeWhiteSpace(actual).equals(expected);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(encloseQuotes(expectedOriginal));
    }

    public static TypeSafeDiagnosingMatcher<String> equalToNormalizingWhiteSpace(String expected) {
        return new EqualToNormalizingWhiteSpace(expected);
    }
}
