package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.pseudosweep.testresources.matchers.ContainsNormalizingWhiteSpace.containsNormalizingWhiteSpace;

class SourceFileInstrumenterTest {

    final String OPERATOR_SET = "sdl";
    final boolean SKIP_TRIVIAL = false;

    @Test
    public void declarations() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        int num1 = 1, num2 = 3;

                        int one = 1;

                        int two;
                        two = one + one;

                        final int four = two + two;

                        String example = new String();

                    }
                }
                """;
        String expectedInstrumented = """
                int num1 = 0;
                if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 1, "com.test.Test", "sdl"))
                    num1 = 1;
                int num2 = 0;
                if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 2, "com.test.Test", "sdl"))
                    num2 = 3;
                int one = 0;
                if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 3, "com.test.Test", "sdl"))
                    one = 1;
                int two = 0;
                if (org.pseudosweep.I.exec("EXPRESSION", 0, "com.test.Test", "sdl")) {
                    two = one + one;
                }
                final int four = two + two;
                String example = "";
                if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 4, "com.test.Test", "sdl"))
                    example = new String();
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void ifThen() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        if(a > b){
                            doSomething();
                        }
                    }
                }
                """;
        String expectedInstrumented = """
                 if (org.pseudosweep.I.exec("IF", 0, "com.test.Test", "sdl")) {
                     if (org.pseudosweep.I.fix(org.pseudosweep.I.eval() && a > b, "IF", 0, "com.test.Test")) {
                         if (org.pseudosweep.I.exec("EXPRESSION", 1, "com.test.Test", "sdl")) {
                             doSomething();
                         }
                     }
                 }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void ifThenElse() {
        String original = """
                package com.test;
                public class Test {
                    public int test() {
                        if (a < b) {
                            doSomething();
                        } else {
                            return a;
                        }
                    return b;
                    }
                }
                """;
        String expectedInstrumented = """
                public int test() {
                    if (org.pseudosweep.I.exec("IF", 0, "com.test.Test", "sdl")) {
                        if (org.pseudosweep.I.fix(org.pseudosweep.I.eval() && a < b, "IF", 0, "com.test.Test")) {
                            if (org.pseudosweep.I.exec("EXPRESSION", 2, "com.test.Test", "sdl")) {
                                doSomething();
                            }
                        } else {
                            if (org.pseudosweep.I.exec("RETURN", 1, "com.test.Test", "sdl")) {
                                return a;
                            }
                            return org.pseudosweep.I.defaultValue("RETURN", 1, "com.test.Test", "sdl") ? 0 : 1;
                        }
                    }
                    if (org.pseudosweep.I.exec("RETURN", 3, "com.test.Test", "sdl")) {
                        return b;
                    }
                    return org.pseudosweep.I.defaultValue("RETURN", 3, "com.test.Test", "sdl") ? 0 : 1;
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }
    @Test
    public void ifThenElseIf() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        if (a < b) {
                            doSomething();
                        } else if (b > a){
                            doSomethingDifferent();
                        } else {
                            doSomethingElse();
                        }
                    }
                }
                """;
        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("IF", 0, "com.test.Test", "sdl")) {
                     if (org.pseudosweep.I.fix(org.pseudosweep.I.eval() && a < b, "IF", 0, "com.test.Test")) {
                         if (org.pseudosweep.I.exec("EXPRESSION", 4, "com.test.Test", "sdl")) {
                             doSomething();
                         }
                     } else if (org.pseudosweep.I.exec("IF", 1, "com.test.Test", "sdl")) {
                         if (org.pseudosweep.I.fix(org.pseudosweep.I.eval() && b > a, "IF", 1, "com.test.Test")) {
                             if (org.pseudosweep.I.exec("EXPRESSION", 3, "com.test.Test", "sdl")) {
                                 doSomethingDifferent();
                             }
                         } else {
                             if (org.pseudosweep.I.exec("EXPRESSION", 2, "com.test.Test", "sdl")) {
                                 doSomethingElse();
                             }
                         }
                     }
                 }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    public void forStatement() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        for (int i = 0; i < b; i++){
                            doSomething();
                        }
                    }
                }
                """;
        String expectedInstrumented = """
                 public void test() {
                    if (org.pseudosweep.I.exec("FOR", 0, "com.test.Test", "sdl")) {
                        for (int i = 0; org.pseudosweep.I.fix(org.pseudosweep.I.eval() && i < b, "LOOP", 0, "com.test.Test"); i++) {
                            if (org.pseudosweep.I.exec("EXPRESSION", 1, "com.test.Test", "sdl")) {
                                doSomething();
                            }
                        }
                    }
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void enhancedFor() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        for (String s : strings()) {
                            something();
                        }
                    }
                }
                """;
        String expectedInstrumented = """
                public void test() {
                     if (org.pseudosweep.I.exec("FOR_EACH", 0, "com.test.Test", "sdl")) {
                         for (String s : strings()) {
                             if (org.pseudosweep.I.exec("EXPRESSION", 1, "com.test.Test", "sdl")) {
                                 something();
                             }
                         }
                     }
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void whileStatement() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        while (a>b) {
                            doSomething();
                        }
                    }
                }
                """;
        String expectedInstrumented = """
                public void test() {
                    if (org.pseudosweep.I.exec("WHILE", 0, "com.test.Test", "sdl")) {
                        while (org.pseudosweep.I.fix(org.pseudosweep.I.eval() && a > b, "LOOP", 0, "com.test.Test")) {
                            if (org.pseudosweep.I.exec("EXPRESSION", 1, "com.test.Test", "sdl")) {
                                doSomething();
                            }
                        }
                    }
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void doWhile() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        do {
                            doSomething();
                        } while (a > b);
                    }
                }
                """;
        String expectedInstrumented = """
                public void test() {
                    if (org.pseudosweep.I.exec("DO", 0, "com.test.Test", "sdl")) {
                        do {
                            if (org.pseudosweep.I.exec("EXPRESSION", 1, "com.test.Test", "sdl")) {
                                doSomething();
                            }
                        } while (org.pseudosweep.I.fix(org.pseudosweep.I.eval() && a > b, "LOOP", 0, "com.test.Test"));
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
                    public void test() {
                       switch(expression) {
                          case x:
                            int a = 0, b = 1, c;
                            doSomething();
                          default:
                            String x, y = "cat", z;
                            a = 2;
                            doDefault();
                        }
                    }
                }
                """;
        String expectedInstrumented = """
                    public void test() {
                         if (org.pseudosweep.I.exec("SWITCH", 0, "com.test.Test", "sdl")) {
                             switch(expression) {
                                 case x:
                                     int a = 0;
                                     if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 1, "com.test.Test", "sdl"))
                                         a = 0;
                                     int b = 0;
                                     if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 2, "com.test.Test", "sdl"))
                                         b = 1;
                                     int c = 0;
                                     if (org.pseudosweep.I.exec("EXPRESSION", 5, "com.test.Test", "sdl")) {
                                         doSomething();
                                     }
                                 default:
                                     String x = "";
                                     String y = "";
                                     if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 3, "com.test.Test", "sdl"))
                                         y = "cat";
                                     String z = "";
                                     a = org.pseudosweep.I.exec("SWITCH_ENTRY_ASSIGNMENT", 4, "com.test.Test", "sdl") ? (int) (2) : 0;
                                     if (org.pseudosweep.I.exec("EXPRESSION", 6, "com.test.Test", "sdl")) {
                                         doDefault();
                                     }
                             }
                         }
                     }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void throwStatement() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        throw new Exception();
                    }
                }
                """;
        String expectedInstrumented = """
                public void test() {
                    if (org.pseudosweep.I.exec("THROW", 0, "com.test.Test", "sdl")) {
                        throw new Exception();
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
                    public void test() {
                        try {
                            somethingPotentiallyRisky();
                        } catch (Exception e) {
                            System.out.println("Caught Exception");
                        }
                    }
                }
                """;
        String expectedInstrumented = """
                public void test() {
                    if (org.pseudosweep.I.exec("TRY", 0, "com.test.Test", "sdl")) {
                        try {
                            if (org.pseudosweep.I.exec("EXPRESSION", 2, "com.test.Test", "sdl")) {
                                somethingPotentiallyRisky();
                            }
                        } catch (Exception e) {
                            if (org.pseudosweep.I.exec("EXPRESSION", 1, "com.test.Test", "sdl")) {
                                System.out.println("Caught Exception");
                            }
                        }
                    }
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void returnPrimitive() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        doSomething();
                        return 10;
                    }
                }
                """;
        String expectedInstrumented = """
                public void test() {
                    if (org.pseudosweep.I.exec("EXPRESSION", 0, "com.test.Test", "sdl")) {
                        doSomething();
                    }
                    if (org.pseudosweep.I.exec("RETURN", 1, "com.test.Test", "sdl")) {
                        return 10;
                    }
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void returnObject() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        doSomething();
                        return "yo";
                    }
                }
                """;
        String expectedInstrumented = """
                public void test() {
                    if (org.pseudosweep.I.exec("EXPRESSION", 0, "com.test.Test", "sdl")) {
                        doSomething();
                    }
                    if (org.pseudosweep.I.exec("RETURN", 1, "com.test.Test", "sdl")) {
                        return "yo";
                    }
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void continueStatement() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        for (int num : numbers) {
                            if (num % 2 != 0) {
                                continue;
                            }
                            lastEvenNumber = num;
                        }
                    }
                }
                """;
        String expectedInstrumented = """
                public void test() {
                    if (org.pseudosweep.I.exec("FOR_EACH", 0, "com.test.Test", "sdl")) {
                        for (int num : numbers) {
                            if (org.pseudosweep.I.exec("IF", 1, "com.test.Test", "sdl")) {
                                if (org.pseudosweep.I.fix(org.pseudosweep.I.eval() && num % 2 != 0, "IF", 0, "com.test.Test")) {
                                    if (org.pseudosweep.I.exec("CONTINUE", 2, "com.test.Test", "sdl")) {
                                        continue;
                                    }
                                }
                            }
                            if (org.pseudosweep.I.exec("EXPRESSION", 3, "com.test.Test", "sdl")) {
                                lastEvenNumber = num;
                            }
                        }
                    }
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void lambdaStatements() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        Function<Integer, Integer> square = (x) -> {
                            int resultSquare = x * x;
                            return resultSquare;
                        };
                    }
                }
                """;
        String expectedInstrumented = """
                public void test() {
                    Function<Integer, Integer> square = null;
                    if (org.pseudosweep.I.exec("LAMBDA", 0, "com.test.Test", "sdl")) {
                        square = (x) -> {
                            int resultSquare = x * x;
                            return resultSquare;
                        };
                    }
                }
                  """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void arrayInitializers() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        int[] intArray1 = new int[5];
                        int[] intArray2 = {1, 2, 3, 4, 5};
                        double[][] doubleMatrix = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}};
                        int[][] jaggedArray = {
                                {1, 2, 3},
                                {4, 5},
                                {6, 7, 8, 9}
                        };
                    }
                }
                """;
        String expectedInstrumented = """
                public void test() {
                    int[] intArray1 = new int[] {};
                    if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 0, "com.test.Test", "sdl"))
                        intArray1 = new int[5];
                    int[] intArray2 = new int[] {};
                    if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 1, "com.test.Test", "sdl"))
                        intArray2 = new int[] { 1, 2, 3, 4, 5 };
                    double[][] doubleMatrix = new double[][] {};
                    if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 2, "com.test.Test", "sdl"))
                        doubleMatrix = new double[][] { { 1.0, 2.0, 3.0 }, { 4.0, 5.0, 6.0 } };
                    int[][] jaggedArray = new int[][] {};
                    if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 3, "com.test.Test", "sdl"))
                        jaggedArray = new int[][] { { 1, 2, 3 }, { 4, 5 }, { 6, 7, 8, 9 } };
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void finalModifiers() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        final int newBufferSize;
                        if (i > 0) {
                            newBufferSize = i;
                            int[] array = {1, 2, 3, 4, 5, 6, 7, 8};
                            Arrays.stream(array).forEach(number -> number = number + newBufferSize);
                        } else {
                            newBufferSize = Math.max(3, 5);
                        }
                    }
                }
                """;
        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("LAMBDA", 3, "com.test.Test", "sdl")) {
                    final int[] array_pseudosweep = array;
                    final int newBufferSize_pseudosweep = newBufferSize;
                    Arrays.stream(array_pseudosweep).forEach(number -> number = number + newBufferSize_pseudosweep);
                    newBufferSize = newBufferSize_pseudosweep;
                    array = array_pseudosweep;
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void finalSplitAssignments() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        final int i;
                        if(a>b){
                            i = 0;
                        } else {
                            i = 1;
                        }
                    }
                }
                """;
        String expectedInstrumented = """
                public void test() {
                    int i = 0;
                    if (org.pseudosweep.I.exec("IF", 0, "com.test.Test", "sdl")) {
                        if (org.pseudosweep.I.fix(org.pseudosweep.I.eval() && a > b, "IF", 0, "com.test.Test")) {
                            if (org.pseudosweep.I.exec("EXPRESSION", 2, "com.test.Test", "sdl")) {
                                i = 0;
                            }
                        } else {
                            if (org.pseudosweep.I.exec("EXPRESSION", 1, "com.test.Test", "sdl")) {
                                i = 1;
                            }
                        }
                    }
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void labels() {
        String original = """
                package com.test;
                public class Test {
                    public void test() {
                        X:
                        while (true) {
                            if (num > 2) {
                                for (int i = 0; i < 10; i++) {
                                    break X;
                                }
                            }
                        }
                    }
                }
                """;
        String expectedInstrumented = """
                public void test() {
                    if (org.pseudosweep.I.exec("WHILE", 0, "com.test.Test", "sdl")) {
                        X: while (true) {
                            if (org.pseudosweep.I.exec("IF", 1, "com.test.Test", "sdl")) {
                                if (org.pseudosweep.I.fix(org.pseudosweep.I.eval() && num > 2, "IF", 0, "com.test.Test")) {
                                    if (org.pseudosweep.I.exec("FOR", 2, "com.test.Test", "sdl")) {
                                        for (int i = 0; org.pseudosweep.I.fix(org.pseudosweep.I.eval() && i < 10, "LOOP", 1, "com.test.Test"); i++) {
                                            if (org.pseudosweep.I.exec("BREAK", 3, "com.test.Test", "sdl")) {
                                                break X;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void innerClassesInReturn() {
        String original = """
                package com.test;
                public class Test {
                    public class OuterClass {
                        public SomeInterface createInnerClass() {
                            return new SomeInterface() {
                                @Override
                                public void doSomething() {
                                    System.out.println("Doing something from inner class.");
                                }
                            };
                        }
                    }
                }
                """;
        String expectedInstrumented = """
                if (org.pseudosweep.I.exec("INNER_CLASS_RETURN", 1, "com.test.Test$OuterClass", "sdl")) {
                     return new SomeInterface() {

                         @Override
                         public void doSomething() {
                             if (org.pseudosweep.I.exec("EXPRESSION", 0, "com.test.Test$OuterClass", "sdl")) {
                                 System.out.println("Doing something from inner class.");
                             }
                         }
                     };
                 }
                return org.pseudosweep.I.defaultValue("RETURN", 1, "com.test.Test$OuterClass", "sdl") ? null : null;
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void innerClassesInVarDeclaration() {
        String original = """
                package com.test;
                public class Test {
                    public void method() {
                        int positive = +1;
                        final int negative = -1;
                        SortedSet<Thing> things = new TreeSet<Thing>(new Comparator<Thing>() {


                            public int compare(Thing one, Thing two) {
                                return 1 * positive;
                            }

                        });
                    }
                }
                """;
        String expectedInstrumented = """
                int positive = 0;
                if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 1, "com.test.Test", "sdl"))
                    positive = +1;
                final int negative = -1;
                SortedSet<Thing> things = null;
                if (org.pseudosweep.I.exec("INNER_CLASS", 2, "com.test.Test", "sdl")) {
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
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }
    @Test
    public void lambdaInVarDeclaration1() {
        String original = """
                package com.test;
                 public class Test {
                     public void method() {
                          int positive = +1;
                          final int negative = -1;
                          List<Integer> numbers = (() -> {
                                     List<Integer> numberList = new ArrayList<>();
                                     numberList.add(positive);
                                     numberList.add(negative);
                                     return numberList;
                                 }).get();
                     }
                 }
                """;
        String expectedInstrumented = """
                int positive = 0;
                if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 0, "com.test.Test", "sdl"))
                    positive = +1;
                final int negative = -1;
                List<Integer> numbers = null;
                if (org.pseudosweep.I.exec("LAMBDA", 1, "com.test.Test", "sdl")) {
                    final int positive_pseudosweep = positive;
                    numbers = (() -> {
                        List<Integer> numberList = new ArrayList<>();
                        numberList.add(positive_pseudosweep);
                        numberList.add(negative);
                        return numberList;
                    }).get();
                    positive = positive_pseudosweep;
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void lambdaInVarDeclaration2() {
        String original = """
                package com.test;
                 public class Test {
                     public void method() {
                          int positive = +1;
                          final int negative = -1;
                          Set<State> x = allTransitions.stream().filter(tr -> a.contains(tr.dest) && tr.values.intersects(interval)).map(tr -> tr.orig).collect(Collectors.toSet());

                     }
                 }
                """;
        String expectedInstrumented = """
                int positive = 0;
                if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 0, "com.test.Test", "sdl"))
                    positive = +1;
                final int negative = -1;
                Set<State> x = null;
                if (org.pseudosweep.I.exec("LAMBDA", 1, "com.test.Test", "sdl")) {
                    x = allTransitions.stream().filter(tr -> a.contains(tr.dest) && tr.values.intersects(interval)).map(tr -> tr.orig).collect(Collectors.toSet());
                }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    public void testSyncBlockInstrumentation() {
        String original = """
                package com.test;
                                
                public class Test {
                    public class TestClass {
                        public static int instrumentSyncBlocks() {
                            Object value = 1;
                            synchronized (value) {
                                return 1;
                            }
                        }
                    }
                }
                """;

        String expectedInstrumented = """
                public static int instrumentSyncBlocks() {
                            Object value = null;
                            if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 0, "com.test.Test$TestClass", "sdl"))
                                value = 1;
                            synchronized (value) {
                                return 1;
                            }
                        }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }


    void assertInstrumentedContainsIgnoringWhiteSpace(String original, String expectedInstrumented) {
        CompilationUnit cu = StaticJavaParser.parse(original);
        SourceFileInstrumenter sfi = new SourceFileInstrumenter("Test.java", cu, OPERATOR_SET, SKIP_TRIVIAL);
        String instrumented = sfi.instrument();
//        System.out.println(instrumented);
        assertThat(instrumented, containsNormalizingWhiteSpace(expectedInstrumented));
    }
}
