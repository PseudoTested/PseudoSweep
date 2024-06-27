package org.pseudosweep.instrumentation;

import com.github.javaparser.ast.CompilationUnit;
import org.pseudosweep.program.ClassUnderTest;

import java.util.*;

public abstract class SourceFileInstrumenter {

    protected final CompilationUnit compilationUnit;
    protected final String fileName;
    protected final String operatorSet;
    protected final boolean skipTrivial;

    protected String packageName;
    protected final Stack<ClassUnderTest> classesBeingParsed;
    protected final Set<ClassUnderTest> classesParsed;

    protected final List<String> warnings;

    public SourceFileInstrumenter(String fileName, CompilationUnit compilationUnit, String operatorSet, boolean skipTrivial) {
        this.fileName = fileName;
        this.compilationUnit = compilationUnit;
        this.operatorSet = operatorSet;
        this.skipTrivial = skipTrivial;


        packageName = "";
        classesBeingParsed = new Stack<>();
        classesParsed = new HashSet<>();

        warnings = new ArrayList<>();
    }

    public String instrument() {
        return null;
    }

    public List<String> getWarnings() {
        return null;
    }

    public Set<ClassUnderTest> getClassesParsed() {
        return null;
    }
}
