package org.pseudosweep.instrumentation.xmt;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.Visitable;
import org.junit.jupiter.api.Test;
import org.pseudosweep.program.Block;
import org.pseudosweep.program.ClassUnderTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.pseudosweep.testresources.matchers.EqualToNormalizingWhiteSpace.equalToNormalizingWhiteSpace;

public class BlockInstrumenterTest {

    final String OPERATOR_SET = "xmt";

    @Test
    public void instrumentMethodBody_nonEmpty() {
        String original = "{ doSomething(); }";

        String expectedInstrumented = """
                {
                    if (org.pseudosweep.I.exec("METHOD", 0, "com.test.Test", "xmt")) {
                        doSomething();
                    }
                    return org.pseudosweep.I.defaultValue("METHOD", 0, "com.test.Test", "xmt") ? null : null;
                }
                """;

        ClassUnderTest classUnderTest = new ClassUnderTest("Test.java", "com.test", "Test");
        BlockInstrumenter blockInstrumenter = new BlockInstrumenter(Block.Type.METHOD, classUnderTest, OPERATOR_SET);
        String instrumented = blockInstrumenter.instrument(StaticJavaParser.parseBlock(original), new ClassOrInterfaceType()).toString();

        assertThat(instrumented, equalToNormalizingWhiteSpace(expectedInstrumented));
    }

    @Test
    public void instrumentMethodBody_empty() {
        String original = "{ }";
        String expectedInstrumented = """
                {
                      org.pseudosweep.I.log("METHOD", 0, "com.test.Test", "xmt");
                }
                 """;

        ClassUnderTest classUnderTest = new ClassUnderTest("Test.java", "com.test", "Test");
        BlockInstrumenter blockInstrumenter = new BlockInstrumenter(Block.Type.METHOD, classUnderTest, OPERATOR_SET);
        String instrumented = blockInstrumenter.instrument(StaticJavaParser.parseBlock(original), new VoidType()).toString();

        assertThat(instrumented, equalToNormalizingWhiteSpace(expectedInstrumented));
    }

    @Test
    public void instrumentConstructorBody_withThis_nonEmpty() {
        String original = """
                package com.test;
                public class Test {
                    public Test() {
                        this();
                        doSomething();
                    }
                }""";

        String expectedInstrumented = """
                {
                    this();
                    if (org.pseudosweep.I.exec("CONSTRUCTOR", 0, "com.test.Test", "xmt")) {
                        doSomething();
                    }
                }
                """;

        ClassUnderTest classUnderTest = new ClassUnderTest("Test.java", "com.test", "Test");
        BlockInstrumenter blockInstrumenter = new BlockInstrumenter(Block.Type.CONSTRUCTOR, classUnderTest, OPERATOR_SET);
        String instrumented = blockInstrumenter.instrument(ExtractConstructorBody.getBlockStmt(original)).toString();

        assertThat(instrumented, equalToNormalizingWhiteSpace(expectedInstrumented));
    }

    @Test
    public void instrumentConstructorBody_withSuper_nonEmpty() {
        String original = """
                package com.test;
                public class Test {
                    public Test() {
                        super();
                        doSomething();
                    }
                }""";

        String expectedInstrumented = """
                {
                    super();
                    if (org.pseudosweep.I.exec("CONSTRUCTOR", 0, "com.test.Test", "xmt")) {
                        doSomething();
                    }
                }
                """;

        ClassUnderTest classUnderTest = new ClassUnderTest("Test.java", "com.test", "Test");
        BlockInstrumenter blockInstrumenter = new BlockInstrumenter(Block.Type.CONSTRUCTOR, classUnderTest, OPERATOR_SET);
        String instrumented = blockInstrumenter.instrument(ExtractConstructorBody.getBlockStmt(original)).toString();

        assertThat(instrumented, equalToNormalizingWhiteSpace(expectedInstrumented));
    }

    @Test
    public void instrumentConstructorBody_withSuper_empty() {
        String original = """
                package com.test;
                public class Test {
                    public Test() {
                        super();
                    }
                }""";

        String expectedInstrumented = """
                {
                    super();
                     org.pseudosweep.I.log("CONSTRUCTOR", 0, "com.test.Test", "xmt");
                }
                """;

        ClassUnderTest classUnderTest = new ClassUnderTest("Test.java", "com.test", "Test");
        BlockInstrumenter blockInstrumenter = new BlockInstrumenter(Block.Type.CONSTRUCTOR, classUnderTest, OPERATOR_SET);
        String instrumented = blockInstrumenter.instrument(ExtractConstructorBody.getBlockStmt(original)).toString();

        assertThat(instrumented, equalToNormalizingWhiteSpace(expectedInstrumented));
    }

    @Test
    public void instrumentConstructorBody_empty() {
        String original = """
                package com.test;
                public class Test {
                    public Test() {
                    }
                }""";

        String expectedInstrumented = """
                {\s
                     org.pseudosweep.I.log("CONSTRUCTOR", 0, "com.test.Test", "xmt");\s
                }""";

        ClassUnderTest classUnderTest = new ClassUnderTest("Test.java", "com.test", "Test");
        BlockInstrumenter blockInstrumenter = new BlockInstrumenter(Block.Type.CONSTRUCTOR, classUnderTest, OPERATOR_SET);
        String instrumented = blockInstrumenter.instrument(ExtractConstructorBody.getBlockStmt(original)).toString();

        assertThat(instrumented, equalToNormalizingWhiteSpace(expectedInstrumented));
    }

    /**
     * This is used when parsing constructor bodies fails via the usual parse route
     */
    private static class ExtractConstructorBody {

        static BlockStmt getBlockStmt(String code) {
            CompilationUnit cu = StaticJavaParser.parse(code);
            Visitor v = new Visitor();
            v.visit(cu, null);
            return v.blockStmt;
        }

        static class Visitor extends GenericVisitorAdapter<Visitable, Void> {
            BlockStmt blockStmt;

            @Override
            public Visitable visit(BlockStmt node, Void arg) {
                this.blockStmt = node;
                return node;
            }
        }
    }
}
