package org.pseudosweep.program;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ClassUnderTestTest {

    @Test
    public void fullClassName() {
        ClassUnderTest classUnderTest = new ClassUnderTest("Test.java", "my.super.cool.package", "Class");
        assertThat(classUnderTest.getFullClassName(), equalTo("my.super.cool.package.Class"));
    }

    @Test
    public void serializeAndDeserialize() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        ClassUnderTest classUnderTest = new ClassUnderTest("File.java", "com.test", "Test");
        Stmt Stmt1 = new Stmt(Stmt.Type.EXPRESSION, 1, "Test", true);
        Stmt Stmt2 = new Stmt(Stmt.Type.EXPRESSION, 2, "Test", true);
        classUnderTest.addCoverageElement(Stmt1);
        classUnderTest.addCoverageElement(Stmt2);
        SourceFilePosition sourceFilePosition = new SourceFilePosition(1, 1, 2, 2 );
        classUnderTest.setPosition(Stmt1, sourceFilePosition);

        String json = objectMapper.writeValueAsString(classUnderTest);

        ClassUnderTest in = objectMapper.readValue(json, ClassUnderTest.class);
        assertThat(in.getCoverageElements().size(), equalTo(2));
        assertThat(in.getCoverageElements().contains(Stmt2), equalTo(true));
        assertThat(in.getPosition(Stmt1), equalTo(sourceFilePosition));
    }

}
