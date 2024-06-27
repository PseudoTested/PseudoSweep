package org.pseudosweep.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static List<String> getFilesWithExtension(String directory, String extension, boolean recurse) throws IOException {
        return getFiles(directory, s -> s.endsWith(extension), recurse);
    }

    public static List<String> getFiles(String directory, Predicate<String> predicate, boolean recurse) throws IOException {
        Path path = Paths.get(directory);
        int maxDepth = recurse ? Integer.MAX_VALUE : 1;
        try (Stream<Path> walk = Files.walk(path, maxDepth)) {
            return walk.map(Path::toString).filter(predicate).collect(Collectors.toList());
        }
    }

    public static List<String> getJavaSourceFiles(String directory, boolean recurse) throws IOException {
        return getFilesWithExtension(directory, JavaConstants.SRC_FILE_EXT, recurse);
    }

    public static List<String> getJavaClassFiles(String directory, boolean recurse) throws IOException {
        return getFilesWithExtension(directory, JavaConstants.CLASS_FILE_EXT, recurse);
    }
}
