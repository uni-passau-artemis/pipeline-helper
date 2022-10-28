package de.uni_passau.fim.se2.pipeline_helper.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

public class CheckerResultWriter {

    private static final Gson GSON = new Gson();

    private CheckerResultWriter() {
    }

    /**
     * Writes the checker results in an Artemis compatible format to the output directory
     *
     * @param outputDirectory the folder to put the results in.
     * @param results         the checker results that should be passed on to Artemis.
     * @throws IOException if outputDirectory exists and is a file or if files cannot be written into it.
     */
    public static void writeFeedback(final Path outputDirectory, final List<CheckerResult> results) throws IOException {
        final boolean outputDirectoryExists = Files.exists(outputDirectory);

        if (!outputDirectoryExists) {
            Files.createDirectories(outputDirectory);
        }
        else if (!Files.isDirectory(outputDirectory)) {
            throw new IOException("Output directory already exists but is a file!");
        }

        for (final CheckerResult result : results) {
            final Path outputFile = Path
                .of(outputDirectory.toString(), String.format("TEST-%s.json", result.getName()));
            Files.writeString(outputFile, GSON.toJson(result));
        }
    }
}
