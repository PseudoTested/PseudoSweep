package org.pseudosweep.program;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SourceFilesUnderTest {

    private Map<String, Set<String>> sourceFileClasses;

    public SourceFilesUnderTest() {
        sourceFileClasses = new HashMap<>();
    }

    public void addClasses(String fileName, Set<ClassUnderTest> classes) {
        sourceFileClasses.putIfAbsent(fileName, new HashSet<>());
        Set<String> classesForFileName = sourceFileClasses.get(fileName);
        classes.forEach(c -> classesForFileName.add(c.getFullClassName()));
    }

    public Map<String, Set<String>> getSourceFileClasses() {
        return sourceFileClasses;
    }

    public void setSourceFileClasses(Map<String, Set<String>> sourceFileClasses) {
        this.sourceFileClasses = sourceFileClasses;
    }

}
