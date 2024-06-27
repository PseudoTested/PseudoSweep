package org.pseudosweep.program;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.pseudosweep.program.comparator.CoverageElementComparator;
import org.pseudosweep.program.serialize.CoverageElementKeyDeserializer;
import org.pseudosweep.util.JavaConstants;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClassUnderTest {
    private final String fileName, packageName, className;
    @JsonDeserialize(keyUsing = CoverageElementKeyDeserializer.class)
    private Map<CoverageElement, SourceFilePosition> coverageElementPositions;

    public ClassUnderTest(@JsonProperty("fileName") String fileName,
                          @JsonProperty("packageName") String packageName,
                          @JsonProperty("className") String className) {
        this.fileName = fileName;
        this.packageName = packageName;
        this.className = className;
        this.coverageElementPositions = new TreeMap<>(new CoverageElementComparator());
    }
    public String getFileName() {
        return fileName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    @JsonIgnore
    public Set<CoverageElement> getCoverageElements() {
        return coverageElementPositions.keySet();
    }

    public Map<CoverageElement, SourceFilePosition> getCoverageElementPositions() {
        return coverageElementPositions;
    }

    public void setCoverageElementPositions(Map<CoverageElement, SourceFilePosition> coverageElementPositions) {
        this.coverageElementPositions = new TreeMap<>(new CoverageElementComparator());
        coverageElementPositions.keySet().forEach(coverageElement -> this.coverageElementPositions.put(coverageElement, coverageElementPositions.get(coverageElement)));
    }

    @JsonIgnore
    public String getFullClassName() {
        return packageName + JavaConstants.PACKAGE_SEPARATOR + className;
    }

    public void addCoverageElement(CoverageElement coverageElement) {
        coverageElementPositions.put(coverageElement, null);
    }

    @JsonIgnore
    public void setPosition(CoverageElement coverageElement, SourceFilePosition position) {
        coverageElementPositions.put(coverageElement, position);
    }

    @JsonIgnore
    public SourceFilePosition getPosition(CoverageElement coverageElement) {
        return coverageElementPositions.get(coverageElement);
    }

    public Set<CoverageElement> find(Predicate<CoverageElement> predicate) {
        return coverageElementPositions.keySet().stream().filter(predicate).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassUnderTest that = (ClassUnderTest) o;
        return getFileName().equals(that.getFileName()) && getPackageName().equals(that.getPackageName()) && getClassName().equals(that.getClassName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileName(), getPackageName(), getClassName());
    }

}
