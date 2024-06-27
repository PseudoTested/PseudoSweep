package org.pseudosweep.instrumentation.sdl;

import com.github.javaparser.StaticJavaParser;
import org.junit.jupiter.api.Test;
import org.pseudosweep.program.ClassUnderTest;
import org.pseudosweep.program.Stmt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.pseudosweep.testresources.matchers.EqualToNormalizingWhiteSpace.equalToNormalizingWhiteSpace;

public class StmtInstrumenterTest {
    final String OPERATOR_SET = "sdl";

    @Test
    void instrument_declaration_assignment_expression() {
        String original = """

                    two = one + one;

                """;
        String expectedInstrumented = """

                    if (org.pseudosweep.I.exec("EXPRESSION", 0,  "com.test.Test", "sdl")) {
                        two = one + one;
                    }

                """;

        ClassUnderTest classUnderTest = new ClassUnderTest("Test.java", "com.test", "Test");
        StmtInstrumenter stmtInstrumenter = new StmtInstrumenter(Stmt.Type.EXPRESSION, classUnderTest, OPERATOR_SET);
        String instrumented = stmtInstrumenter.instrument(StaticJavaParser.parseStatement(original)).toString();

        assertThat(instrumented, equalToNormalizingWhiteSpace(expectedInstrumented));

    }

}

