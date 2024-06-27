package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.pseudosweep.testresources.matchers.ContainsNormalizingWhiteSpace.containsNormalizingWhiteSpace;

class ReturnMutatorInstrumenterTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }


    final String OPERATOR_SET = "sdl";

    @Test
    void instrument_inner_class_method() {
        String original = """
                package com.test;
                public class Test {
                    public String method() {
                        int positive = +1;
                        final int negative = -1;
                        SortedSet<Thing> things = new TreeSet<Thing>(new Comparator<Thing>() {

                            public int compare(Thing one, Thing two) {
                                return 1 * positive;
                            }

                        });
                        return "different from inner class";
                    }
                }
                """;
        String expectedInstrumented = """
                    if (org.pseudosweep.I.exec("INNER_CLASS", 3, "com.test.Test", "sdl")) {
                        final int positive_pseudosweep = positive;
                        things = new TreeSet<Thing>(new Comparator<Thing>() {
                        
                            public int compare(Thing one, Thing two) {
                                if (org.pseudosweep.I.exec("RETURN", 0, "com.test.Test", "sdl")) {
                                    return 1 * positive_pseudosweep;
                                }
                                return org.pseudosweep.I.defaultValue("RETURN", 0, "com.test.Test", "sdl") ? 0 : 1;
                            }
                        });
                        positive = positive_pseudosweep;
                    }
                    if (org.pseudosweep.I.exec("RETURN", 1, "com.test.Test", "sdl")) {
                        return "different from inner class";
                    }
                    return org.pseudosweep.I.defaultValue("RETURN", 1, "com.test.Test", "sdl") ? "" : "A";
                }
                   """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    void assertInstrumentedContainsIgnoringWhiteSpace(String original, String expectedInstrumented) {
        CompilationUnit cu = StaticJavaParser.parse(original);
        SourceFileInstrumenter sfi = new SourceFileInstrumenter("Test.java", cu, OPERATOR_SET, false);
        String instrumented = sfi.instrument();
//        System.out.println(instrumented);
        assertThat(instrumented, containsNormalizingWhiteSpace(expectedInstrumented));
    }
}
