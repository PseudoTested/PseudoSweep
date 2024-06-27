package org.pseudosweep.instrumentation.xmt;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.pseudosweep.testresources.matchers.ContainsNormalizingWhiteSpace.containsNormalizingWhiteSpace;

class SourceFileInstrumenterTest {

    final String OPERATOR_SET = "xmt";
    final boolean SKIP_TRIVIAL = false;


    @Test
    public void nestedClasses() {

        String original = """
                package com.test;

                public class Outer {

                    public class Inner {
                        void method() {
                            doSomething();
                        }
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Outer$Inner", "xmt")) {
                    doSomething();
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void constructorBlocks() {
        String original = """
                package com.test;
                public class Test {
                    public Test() {
                    }

                    public Test(int a) {
                        super(a);
                        doSomething();
                    }

                    public Test(int a, int b) {
                        this();
                    }
                }""";


        String expectedInstrumented = """
                public Test() {
                    org.pseudosweep.I.log("CONSTRUCTOR", 0, "com.test.Test", "xmt");
                }

                public Test(int a) {
                    super(a);
                    if (org.pseudosweep.I.exec("CONSTRUCTOR", 1, "com.test.Test", "xmt")) {
                        doSomething();
                    }
                }

                public Test(int a, int b) {
                    this();
                    org.pseudosweep.I.log("CONSTRUCTOR", 2, "com.test.Test", "xmt");
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void ifElseThenBlocks() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        if (a > b && !(c == 10 || e == f)) {
                            doSomething();
                        } else {
                            doSomethingElse();
                        }
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                    if (a > b && !(c == 10 || e == f)) {
                        doSomething();
                    } else {
                        doSomethingElse();
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void ifElseThenBlocks_elseNotPresent() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        if (a > b) {
                            doSomething();
                        }
                        if (a == b) {
                            doSomethingElse();
                        }
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                        if (a > b) {
                            doSomething();
                        }
                        if (a == b) {
                            doSomethingElse();
                        }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void methodBlock_returnChar() {
        String original = """
                package com.test;
                public class Test {
                    public char test() {
                        doSomething();
                        return 'a';
                    }
                }
                """;

        String expectedInstrumented = """
                public char test() {
                    if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                        doSomething();
                        return 'a';
                    }
                   return org.pseudosweep.I.defaultValue("METHOD", 0, "com.test.Test", "xmt") ? ' ' : 'A';
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void methodBlock_returnInt() {
        String original = """
                package com.test;
                public class Test {
                    public int test() {
                        doSomething();
                        return 10;
                    }
                }
                """;

        String expectedInstrumented = """
                public int test() {
                    if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                        doSomething();
                        return 10;
                    }
                    return org.pseudosweep.I.defaultValue("METHOD", 0, "com.test.Test", "xmt") ? 0 : 1;
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void methodBlock_returnStringObject() {
        String original = """
                package com.test;
                public class Test {
                    public String test() {
                        doSomething();
                        return "yo";
                    }
                }
                """;

        String expectedInstrumented = """
                public String test() {
                    if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                        doSomething();
                        return "yo";
                    }
                    return org.pseudosweep.I.defaultValue("METHOD", 0, "com.test.Test", "xmt") ? "" : "A";
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void synchronizedBlock() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        synchronized(someGlobalVar) {
                            doSomething();
                        }
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                    synchronized (someGlobalVar) {
                            doSomething();
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void tryCatch() {
        String original = """
                        package com.test;

                        public class Test {
                            public static void main(String[] args) {
                                try {
                                    somethingPotentiallyRisky();
                                } catch (Exception e) {
                                    System.out.println("Caught Exception");
                                }
                            }
                        }
                """;


        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                    try {
                            somethingPotentiallyRisky();
                    } catch (Exception e) {
                            System.out.println("Caught Exception");
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void containingClassNamesInClasses() {

        String original = """
                package com.test;

                public class Outer {

                    public class Inner {

                        enum MyEnum {
                            ONE, TWO;

                            MyEnum() {
                                doSomething();
                            }
                        }

                        public Inner() {
                            doSomething();
                        }
                    }

                    public Outer() {
                        doSomething();
                    }
                }
                """;

        String expectedInstrumented = """
                public class Outer {

                    public class Inner {

                        enum MyEnum {

                            ONE, TWO;

                            MyEnum() {
                                if (org.pseudosweep.I.exec("CONSTRUCTOR", 0, "com.test.Outer$Inner$MyEnum", "xmt")) {
                                    doSomething();
                                }
                            }
                        }

                        public Inner() {
                            if (org.pseudosweep.I.exec("CONSTRUCTOR", 0, "com.test.Outer$Inner", "xmt")) {
                                doSomething();
                            }
                        }
                    }

                    public Outer() {
                        if (org.pseudosweep.I.exec("CONSTRUCTOR", 0, "com.test.Outer", "xmt")) {
                            doSomething();
                        }
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void initializerDeclaration() {

        String original = """
                package com.test;

                public class Test {

                    {
                        someInitialization();
                    }

                    // ... rest class goes here
                }
                """;

        String expectedInstrumented = """
                {
                    if (org.pseudosweep.I.exec("INITIALIZER", 0, "com.test.Test", "xmt")) {
                        someInitialization();
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void ternary() {

        String original = """
                package com.test;

                public class Test {
                    boolean method() {
                        return someCondition() ? doSomething() : doSomethingElse();
                    }
                }
                """;

        String expectedInstrumented = """
                 boolean method() {
                    if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                        return someCondition() ? doSomething() : doSomethingElse();
                    }
                 return org.pseudosweep.I.defaultValue("METHOD", 0, "com.test.Test", "xmt") ? false : true;                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void doLoop() {

        String original = """
                package com.test;

                public class Test {
                    void method() {
                        do {
                            something();
                        } while (condition());
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                    do {
                        something();
                    } while (condition());
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void whileLoop() {

        String original = """
                package com.test;

                public class Test {
                    void method() {
                        while (condition()) {
                            something();
                        }
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                    while (condition()) {
                            something();
                        }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void forLoop() {

        String original = """
                package com.test;

                public class Test {
                    void method() {
                        for (int i = 0; i < 5; i++) {
                            something();
                        }
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                    for (int i = 0; i < 5; i++) {
                            something();
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void forEachLoop() {

        String original = """
                package com.test;

                public class Test {
                    void method() {
                        for (String s : strings()) {
                            something();
                        }
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                    for (String s : strings()) {
                        something();
                    }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void switchStatement() {

        String original = """
                package com.test;

                public class Test {
                    void method() {
                        switch(expression) {
                          case x:
                            int a = 0, b = 1, c;
                            doSomething();
                          default:
                            String x, y = "cat", z;
                            doDefault();
                        }
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                    switch(expression) {
                          case x:
                            int a = 0, b = 1, c;
                            doSomething();
                          default:
                            String x, y = "cat", z;
                            doDefault();
                        }
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void list_new() {

        String original = """
                package com.test;

                public class Test {
                    List<String> getList() {
                        return new ArrayList<>(Arrays.asList("Element 1", "Element 2", "Element 3"));
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                    return new ArrayList<>(Arrays.asList("Element 1", "Element 2", "Element 3"));
                }
                return org.pseudosweep.I.defaultValue("METHOD", 0, "com.test.Test", "xmt") ? null : new java.util.ArrayList();
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void queue_new() {

        String original = """
                package com.test;

                public class Test {
                    Queue<String> getQueue(List<String> list) {
                        return new LinkedList<>(list);
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                    return new LinkedList<>(list);
                }
                return org.pseudosweep.I.defaultValue("METHOD", 0, "com.test.Test", "xmt") ? null : new java.util.LinkedList();
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }
    @Test
    public void set_new() {

        String original = """
                package com.test;

                public class Test {
                    Set<String> method(List<String> list) {
                        return new HashSet<>(list);
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                    return new HashSet<>(list);
                }
                return org.pseudosweep.I.defaultValue("METHOD", 0, "com.test.Test", "xmt") ? null : new java.util.HashSet();

                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void map_new() {

        String original = """
                package com.test;

                public class Test {
                    Map<Integer, String> getMap(List<String> list) {
                        Map<Integer, String> map = new HashMap<>();
                        return map;
                    }
                }
                """;

        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                    Map<Integer, String> map = new HashMap<>();
                    return map;
                }
                return org.pseudosweep.I.defaultValue("METHOD", 0, "com.test.Test", "xmt") ? null : new java.util.HashMap();
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }


    /*
    @Test
    public void newTest() {

        String original = """
                package com.test;

                public class Test {
                    void method() {

                    }
                }
                """;

        String expectedInstrumented = """
                """;

        //assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }
    */

    void assertInstrumentedContainsIgnoringWhiteSpace(String original, String expectedInstrumented) {
        CompilationUnit cu = StaticJavaParser.parse(original);
        SourceFileInstrumenter sfi = new SourceFileInstrumenter("Test.java", cu, OPERATOR_SET, SKIP_TRIVIAL);
        String instrumented = sfi.instrument();

        assertThat(instrumented, containsNormalizingWhiteSpace(expectedInstrumented));
    }
}
