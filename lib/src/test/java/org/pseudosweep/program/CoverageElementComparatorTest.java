package org.pseudosweep.program;

import org.junit.jupiter.api.Test;
import org.pseudosweep.program.comparator.CoverageElementComparator;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class CoverageElementComparatorTest {

    @Test
    public void testStmtAndDecision() {
        Stmt s = new Stmt(Stmt.Type.EXPRESSION, 1, "com.test.Test", true);
        Decision d = new Decision(true, Decision.Type.IF, 1, "com.test.Test");

        List<CoverageElement> coverageElements = new ArrayList<>();
        coverageElements.add(s);
        coverageElements.add(d);
        coverageElements.sort(new CoverageElementComparator());

        assertThat(coverageElements.get(0), equalTo(d));
        assertThat(coverageElements.get(1), equalTo(s));

        coverageElements = new ArrayList<>();
        coverageElements.add(d);
        coverageElements.add(s);
        coverageElements.sort(new CoverageElementComparator());

        assertThat(coverageElements.get(0), equalTo(d));
        assertThat(coverageElements.get(1), equalTo(s));
    }

}
