package de.uni_passau.fim.se2.pipeline_helper.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FilteredFilesStream {
    /**
     * Returns all readable files with the given file extension in the search path.
     *
     * @param searchPath the directory to search for files in
     * @param extension  the file ending to filter on, e.g. "java"
     * @return all readable files with the extension in the search path
     * @throws IOException if the searchPath is not accessible
     */
    public static Stream<Path> files(final Path searchPath, final String extension) throws IOException {
        final String end = String.format(".%s", extension);

        return Files.walk(searchPath).filter(p -> p.getFileName().toString().endsWith(end)).map(Path::toFile)
                .filter(f -> f.isFile() && f.canRead()).map(File::toPath);
    }
}
