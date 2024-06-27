package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pseudosweep.program.ClassUnderTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.pseudosweep.testresources.matchers.EqualToNormalizingWhiteSpace.equalToNormalizingWhiteSpace;

class DeclarationInstrumenterTest {

    final String OPERATOR_SET = "sdl";

    @Test
    void instrument_declaration_multiple_single_statement() {
        String original = """
                {
                    int num1 = 1, num2 = 3;
                }
                """;
        String expectedInstrumented = """
                {
                   int num1 = 0;
                    if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 0, "com.test.Test", "sdl"))
                        num1 = 1;
                    int num2 = 0;
                    if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 1, "com.test.Test", "sdl"))
                        num2 = 3;
                }
                            """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);

    }

    @Test
    void instrument_declaration_single_statement() {
        String original = """
                {
                    int one = 1;
                }
                """;
        String expectedInstrumented = """
                {
                    int one = 0;
                    if (org.pseudosweep.I.exec("VARIABLE_DECLARATION", 0, "com.test.Test", "sdl"))
                        one = 1;
                }
                """;

        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    @Test
    void instrument_inner_class_in_declaration() {
        String original = """
                {
                    final int sign = -1;
                    SortedSet<Thing> things = new TreeSet<Thing>(new Comparator<Thing>() {
                        
                    
                        public int compare(Thing one, Thing two) {
                            return 1 * sign;
                        }
                        
                    });
                }
                """;
        String expectedInstrumented = """
                {     
                     final int sign = -1;
                      SortedSet<Thing> things = null;
                      if (org.pseudosweep.I.exec("INNER_CLASS", 0, "com.test.Test", "sdl")) {
                          things = new TreeSet<Thing>(new Comparator<Thing>() {
                  
                              public int compare(Thing one, Thing two) {
                                  return 1 * sign;
                              }
                          });
                      }
                 }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }


    @Test
    void instrument_lambda_in_declaration() {
        String original = """
                {
                 List<Integer> numbers = (() -> {
                            List<Integer> numberList = new ArrayList<>();
                            numberList.add(1);
                            return numberList;
                        }).get();
                }
                """;
        String expectedInstrumented = """
                {     
                     List<Integer> numbers = null;
                          if (org.pseudosweep.I.exec("LAMBDA", 0, "com.test.Test", "sdl")) {
                              numbers = (() -> {
                                  List<Integer> numberList = new ArrayList<>();
                                  numberList.add(1);
                                  return numberList;
                              }).get();
                          }
                 }
                """;
        assertInstrumentedContainsIgnoringWhiteSpace(original, expectedInstrumented);
    }

    void assertInstrumentedContainsIgnoringWhiteSpace(String original, String expectedInstrumented) {
        ClassUnderTest classUnderTest = new ClassUnderTest("Test.java", "com.test", "Test");
        NodeList<Statement> statements = DeclarationInstrumenter.splitDeclarations(StaticJavaParser.parseBlock(original).getStatements(), classUnderTest, OPERATOR_SET);
        BlockStmt blockStmt = new BlockStmt(statements);
        String instrumented = blockStmt.toString();
//        System.out.println(instrumented);
        assertThat(instrumented, equalToNormalizingWhiteSpace(expectedInstrumented));
    }
}

