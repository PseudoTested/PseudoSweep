package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.pseudosweep.testresources.matchers.ContainsNormalizingWhiteSpace.containsNormalizingWhiteSpace;

class TempFinalInsertionTest {

    final String OPERATOR_SET = "sdl";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void insert_final() {
        String original = """
                package com.test;
                public class Test {
                    public void method() {
                          Map<String, String> settings = new HashMap<>();
                          prop.forEach((key, value) -> settings.put(key.toString(), value.toString()));
                     }
                 }
                """;
        String expectedInstrumented = """
                 Map<String, String> settings = null;
                 if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 1, "com.test.Test", "sdl"))
                     settings = new HashMap<>();
                 if (org.pseudosweep.I.exec("LAMBDA", 0, "com.test.Test", "sdl")) {
                     final Map<String, String> settings_pseudosweep = settings;
                     prop.forEach((key, value) -> settings_pseudosweep.put(key.toString(), value.toString()));
                     settings = settings_pseudosweep;
                 }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    void insert_final_within_if_stmt() {
        String original = """
                package com.test;
                public class Test {
                    public void method() {
                        if(this){
                              Map<String, String> settings = new HashMap<>();
                              prop.forEach((key, value) -> settings.put(key.toString(), value.toString()));
                        }
                     }
                 }
                """;
        String expectedInstrumented = """
                 Map<String, String> settings = null;
                 if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 2, "com.test.Test", "sdl"))
                     settings = new HashMap<>();
                 if (org.pseudosweep.I.exec("LAMBDA", 1, "com.test.Test", "sdl")) {
                     final Map<String, String> settings_pseudosweep = settings;
                     prop.forEach((key, value) -> settings_pseudosweep.put(key.toString(), value.toString()));
                     settings = settings_pseudosweep;
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