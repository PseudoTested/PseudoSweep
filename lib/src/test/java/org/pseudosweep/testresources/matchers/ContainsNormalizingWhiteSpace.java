package org.pseudosweep.testresources.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static org.pseudosweep.util.StringUtils.encloseQuotes;
import static org.pseudosweep.util.StringUtils.normalizeWhiteSpace;

public class ContainsNormalizingWhiteSpace extends TypeSafeDiagnosingMatcher<String> {

    private final String containedOriginal;
    private final String contained;

    public ContainsNormalizingWhiteSpace(String contained) {
        this.containedOriginal = contained;
        this.contained = normalizeWhiteSpace(contained);
    }

    @Override
    protected boolean matchesSafely(String container, Description description) {
        description.appendText(encloseQuotes(containedOriginal) + "\nis not contained in:\n" + encloseQuotes(container));
        return normalizeWhiteSpace(container).contains(contained);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("The expected string to contained in the actual string, ignoring whitespace.");
        description.appendText(containedOriginal);
    }

    public static TypeSafeDiagnosingMatcher<String> containsNormalizingWhiteSpace(String contained) {
        return new ContainsNormalizingWhiteSpace(contained);
    }
}
