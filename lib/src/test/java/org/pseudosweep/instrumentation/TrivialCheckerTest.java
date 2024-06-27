package org.pseudosweep.instrumentation;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.pseudosweep.testresources.matchers.ContainsNormalizingWhiteSpace.containsNormalizingWhiteSpace;

class TrivialCheckerTest {

    final boolean SKIP_TRIVIAL = true;


    @Test
    void skipSimpleGetters1() {
        String original = """
                package com.test;
                public class Test {
                    public String getAString() {
                        return this.string;
                    }
                }
                """;

        String expected = """
                package com.test;
                public class Test {
                    public String getAString() {
                        return this.string;
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void skipSimpleGetters2() {
        String original = """
                package com.test;
                public class Test {
                    public String getAString() {
                        return string;
                    }
                }
                """;

        String expected = """
                package com.test;
                public class Test {
                    public String getAString() {
                        return string;
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void instrumentNonTrivialGetter() {
        String original = """
                package com.test;
                public class Test {
                    public int getACalculation() {
                        return 1 * 4 * a * b * c;
                    }
                }
                """;
        String expected = """
                 if (org.pseudosweep.I.exec("RETURN", 0, "com.test.Test", "sdl")) {
                     return 1 * 4 * a * b * c;
                 }
                 return org.pseudosweep.I.defaultValue("RETURN", 0, "com.test.Test", "sdl") ? 0 : 1;
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void skipSimpleSetters() {
        String original = """
                package com.test;
                public class Test {
                    public void setAString(String aString) {
                        this.aString = aString;
                    }
                }
                """;

        String expected = """
                package com.test;
                public class Test {
                    public void setAString(String aString) {
                        this.aString = aString;
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);

    }

    @Test
    void instrumentNonTrivialSetter() {
        String original = """
                package com.test;
                public class Test {
                    public void methodName(int a) {
                        this.a = a * b * c;
                    }
                }
                """;
        String expected = """
                if (org.pseudosweep.I.exec("EXPRESSION", 0, "com.test.Test", "sdl")) {
                    this.a = a * b * c;
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void skipDeprecatedMethods() {
        String original = """
                package com.test;
                public class Test {
                    @Deprecated
                    public void test() {
                        doSomething();
                    }
                }
                """;

        String expected = """
                package com.test;
                public class Test {
                    @Deprecated
                    public void test() {
                        doSomething();
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);

    }

    @Test
    void instrumentNonDeprecatedMethod() {
        String original = """
                package com.test;
                public class Test {
                    public String methodName() {
                        return "this" + "is" + "a" + "string" + a;
                    }
                }
                """;
        String expected = """
                if (org.pseudosweep.I.exec("RETURN", 0, "com.test.Test", "sdl")) {
                    return "this" + "is" + "a" + "string" + a;
                }
                return org.pseudosweep.I.defaultValue("RETURN", 0, "com.test.Test", "sdl") ? "" : "A";
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void skipDeprecatedClasses() {
        String original = """
                package com.test;
                @Deprecated
                public class Test {
                    public void test() {
                        doSomething();
                    }
                }
                """;

        String expected = """
                package com.test;
                @Deprecated
                public class Test {
                    public void test() {
                        doSomething();
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);

    }

    @Test
    void instrumentNonDeprecatedMethodOnly() {
        String original = """
                package com.test;
                public class Test {
                    public void methodOne() {
                        return oneString + twoString;
                    }
                    @Deprecated
                    public void methodTwo() {
                        return oneString + twoString;
                    }
                }
                """;
        String expected = """
                public void methodOne() {
                    if (org.pseudosweep.I.exec("RETURN", 0, "com.test.Test", "sdl")) {
                        return oneString + twoString;
                    }
                }

                @Deprecated
                public void methodTwo() {
                    return oneString + twoString;
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void skipReturnsLiteralConstant() {
        String original = """
                package com.test;
                public class Test {
                    public int test() {
                        return 1;
                    }
                }
                """;

        String expected = """
                package com.test;
                public class Test {
                    public int test() {
                        return 1;
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);

    }

    @Test
    void instrumentNonTrivialVariable() {
        String original = """
                package com.test;
                public class Test {
                    public void methodName() {
                        return "";
                    }
                }
                """;
        String expected = """
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

//    @Test
//    void skipToString() {
//        String original = """
//                package com.test;
//                public class Test {
//                    public String toString() {
//                        return "string" + a;
//                    }
//                }
//                """;
//
//        String expected = """
//                package com.test;
//                public class Test {
//                public String toString() {
//                        return "string" + a;
//                    }
//                }
//                """;
//
//        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
//
//    }

    @Test
    void instrumentNonTrivialToString() {
//
//        String original = """
//                package com.test;
//                public class Test {
//                    public void methodName() {
//                        return "";
//                    }
//                }
//                """;
//        String expected = """
//                """;
//
//        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void skipStaticInitializer() {
        String original = """
                package com.test;
                public class Test {
                    static {
                        System.out.println("Static initializer");
                    }
                }
                """;

        String expected = """
                package com.test;
                public class Test {
                    static {
                        System.out.println("Static initializer");
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);

    }

    @Test
    void instrumentNonTrivialInitializer() {
        String original = """
                package com.test;
                public class Test {
                        private int instanceVariable;
                        // Instance initializer block
                        {
                            instanceVariable = 10;
                        }
                }
                """;
        String expected = """
                    private int instanceVariable;

                    // Instance initializer block
                    {
                        if (org.pseudosweep.I.exec("EXPRESSION", 0, "com.test.Test", "sdl")) {
                            instanceVariable = 10;
                        }
                    }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void skipReturnsOnlyThis() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        return this;
                    }
                }
                """;

        String expected = """
                package com.test;
                public class Test {
                    public void test() {
                        return this;
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void instrumentMoreThanThis() {
        String original = """
                package com.test;
                public class Test {
                    public String test() {
                        return this.string + "a string";
                    }
                }
                """;

        String expected = """
                        if (org.pseudosweep.I.exec("RETURN", 0, "com.test.Test", "sdl")) {
                            return this.string + "a string";
                        }
                        return org.pseudosweep.I.defaultValue("RETURN", 0, "com.test.Test", "sdl") ? "" : "A";
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void skipEmptyInitializer() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        return new int[0];
                    }
                }
                """;

        String expected = """
                package com.test;
                public class Test {
                    public void test() {
                        return new int[0];
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void skipEmptyVoidMethod() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {

                    }
                }
                """;

        String expectedSDL = """
                package com.test;
                public class Test {
                    public void test() {

                    }
                }
                """;


        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedSDL);
    }

    @Test
    void instrumentNonEmptyVoidMethod() {
        String original = """
                package com.test;
                public class Test {
                    public void methodName() {
                        int i = 1;
                        int j = k;
                        String s = "string";
                    }
                }
                """;
        String expected = """
                int i = 0;
                if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 0, "com.test.Test", "sdl"))
                    i = 1;
                int j = 0;
                if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 1, "com.test.Test", "sdl"))
                    j = k;
                String s = "";
                if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 2, "com.test.Test", "sdl"))
                    s = "string";
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

//    @Test
//    void skipDelegationMethod() {
//        String original = """
//                package com.test;
//                public class Test {
//                    public void test() {
//                        System.out.println(message);
//                    }
//                }
//                """;
//
//        String expected = """
//                package com.test;
//                public class Test {
//                    public void test() {
//                        System.out.println(message);
//                    }
//                }
//                """;
//
//        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
//    }

    @Test
    void instrumentNonDelegationMethod() {
        String original = """
                 package com.test;
                 public class Test {
                     public void methodName() {
                         this.workerMethod();
                         otherWorkerMethod();
                     }
                 }
                """;
        String expected = """
                if (org.pseudosweep.I.exec("EXPRESSION", 0, "com.test.Test", "sdl")) {
                    this.workerMethod();
                }
                if (org.pseudosweep.I.exec("EXPRESSION", 1, "com.test.Test", "sdl")) {
                    otherWorkerMethod();
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void returnOnlyAParameter() {
        String original = """
                package com.test;
                public class Test {
                    public String test(String s) {
                        return s;
                    }
                }
                """;

        String expected = """
                package com.test;
                public class Test {
                    public String test(String s) {
                        return s;
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);

    }

    @Test
    void instrumentACombinationOfParameters() {
        String original = """
                package com.test;
                public class Test {
                    public String concatenate(String a, String b) {
                        return a + b;
                    }
                }
                """;
        String expected = """
                if (org.pseudosweep.I.exec("RETURN", 0, "com.test.Test", "sdl")) {
                    return a + b;
                }
                return org.pseudosweep.I.defaultValue("RETURN", 0, "com.test.Test", "sdl") ? "" : "A";
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    @Test
    void instrumentParameterCalculation() {
        String original = """
                package com.test;
                public class Test {
                    public String concatenate(String a) {
                        return a + "b";
                    }
                }
                """;
        String expected = """
                if (org.pseudosweep.I.exec("RETURN", 0, "com.test.Test", "sdl")) {
                    return a + "b";
                }
                return org.pseudosweep.I.defaultValue("RETURN", 0, "com.test.Test", "sdl") ? "" : "A";
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expected);
    }

    void assertInstrumentedContainsIgnoringWhiteSpace(String original, String expectedInstrumented) {
        CompilationUnit cu = StaticJavaParser.parse(original);
        SourceFileInstrumenter sfiSDL = new org.pseudosweep.instrumentation.sdl.SourceFileInstrumenter("Test.java", cu, "sdl", SKIP_TRIVIAL);
        String instrumentedSDL = sfiSDL.instrument();
        assertThat(instrumentedSDL, containsNormalizingWhiteSpace(expectedInstrumented));
    }
}
