// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers.file_exists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import de.uni_passau.fim.se2.pipeline_helper.model.Checker;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

/**
 * Checks that all given paths are readable files and are not empty.
 */
public class FileExistsChecker implements Checker {

    private static final String CHECKER_NAME = "FileExistsChecker";

    private final List<Path> toCheck;

    public FileExistsChecker(final List<Path> toCheck) {
        this.toCheck = toCheck;
    }

    @Override
    public CheckerResult check() throws CheckerException {
        final Map<FileStatus, List<Path>> filesByStatus = new HashMap<>();
        Arrays.stream(FileStatus.values()).forEach(status -> filesByStatus.put(status, new ArrayList<>()));

        for (final Path p : toCheck) {
            // 'Files.exists(path)' also returns true for directories, so we need this extra check here
            if (!Files.exists(p) || (Files.exists(p) && Files.isDirectory(p))) {
                filesByStatus.get(FileStatus.MISSING).add(p);
                continue;
            }

            try {
                if (Files.isRegularFile(p) && isFileEmpty(p)) {
                    filesByStatus.get(FileStatus.EMPTY).add(p);
                }
                else {
                    filesByStatus.get(FileStatus.VALID).add(p);
                }
            }
            catch (IOException e) {
                filesByStatus.get(FileStatus.NON_READABLE).add(p);
            }
        }

        final boolean successful = filesByStatus.get(FileStatus.VALID).equals(toCheck);

        if (!successful) {
            final String message = buildFeedbackString(filesByStatus);
            return new CheckerResult(CHECKER_NAME, false, message);
        }
        else {
            return new CheckerResult(CHECKER_NAME, true);
        }
    }

    private static boolean isFileEmpty(final Path p) throws IOException {
        return Files.readString(p).isBlank();
    }

    private String buildFeedbackString(final Map<FileStatus, List<Path>> filesByStatus) {
        final StringBuilder sb = new StringBuilder();

        for (FileStatus status : FileStatus.values()) {
            if (status == FileStatus.VALID) {
                // no error message here
                continue;
            }

            final List<Path> invalidPaths = filesByStatus.get(status);
            if (!invalidPaths.isEmpty()) {
                if (!sb.isEmpty()) {
                    sb.append("\n\n");
                }
                sb.append(status.toString()).append("\n");
                sb.append(
                    invalidPaths.stream()
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .collect(Collectors.joining("\n"))
                );
            }
        }

        return sb.toString();
    }
}
