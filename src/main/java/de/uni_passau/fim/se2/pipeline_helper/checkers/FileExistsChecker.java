package de.uni_passau.fim.se2.pipeline_helper.checkers;

import de.uni_passau.fim.se2.pipeline_helper.model.Checker;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Checks that all given paths are readable files and are not empty.
 */
public class FileExistsChecker implements Checker {
    private final static String CHECKER_NAME = "FileExistsChecker";

    private final List<Path> toCheck;

    public FileExistsChecker(final List<Path> toCheck) {
        this.toCheck = toCheck;
    }

    @Override
    public CheckerResult check() throws CheckerException {
        final List<Path> nonExistant = new ArrayList<>();
        final List<Path> emptyFile = new ArrayList<>();

        for (final Path p : toCheck) {
            if (!Files.exists(p)) {
                nonExistant.add(p);
                continue;
            }

            if (!Files.isRegularFile(p)) {
                continue;
            }

            try {
                if (Files.readString(p).isBlank()) {
                    emptyFile.add(p);
                }
            } catch (IOException ignored) {
                // File cannot be read, assumed to be empty
                emptyFile.add(p);
            }
        }

        final boolean successful = nonExistant.isEmpty() && emptyFile.isEmpty();

        if (!successful) {
            final String message = buildFeedbackString(nonExistant, emptyFile);
            return new CheckerResult(CHECKER_NAME, false, message);
        } else {
            return new CheckerResult(CHECKER_NAME, true);
        }
    }

    private String buildFeedbackString(final List<Path> nonExistantFiles, final List<Path> emptyFiles) {
        final StringBuilder sb = new StringBuilder();

        if (!nonExistantFiles.isEmpty()) {
            sb.append("Missing files:\n");
            sb.append(nonExistantFiles.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining("\n")));
        }
        if (!emptyFiles.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append("\n\n");
            }
            sb.append("Empty or non-readable files:\n");
            sb.append(emptyFiles.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining("\n")));
        }

        return sb.toString();
    }
}
