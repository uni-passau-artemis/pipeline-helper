// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers.line_length;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.pipeline_helper.model.Checker;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

/**
 * Checks that all lines in all files have a maximum length.
 */
public class LineLengthChecker implements Checker {

    private static final String CHECKER_NAME = "LineLengthChecker";

    private final Stream<Path> files;
    private final int maxLength;
    private final Path directory;

    public LineLengthChecker(final Path directory, final Stream<Path> files, final int maxLength) {
        this.files = files;
        this.maxLength = maxLength;
        this.directory = directory;
    }

    @Override
    public CheckerResult check() throws CheckerException {
        final List<FileLineLengthViolations> violations = new ArrayList<>();
        for (Iterator<Path> it = files.iterator(); it.hasNext();) {
            final Path p = it.next();
            final SortedMap<Integer, Integer> linesWithViolations;
            try {
                linesWithViolations = getAllViolationsWithLength(p);
            }
            catch (CheckerException e) {
                return new CheckerResult(CHECKER_NAME, false, e.getMessage());
            }
            if (!linesWithViolations.isEmpty()) {
                violations.add(new FileLineLengthViolations(directory.relativize(p), linesWithViolations));
            }
        }

        if (violations.isEmpty()) {
            return new CheckerResult(CHECKER_NAME, true);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Found files with lines longer than %d characters:%n", maxLength));
        violations.forEach(sb::append);

        return new CheckerResult(CHECKER_NAME, false, sb.toString().trim());
    }

    private SortedMap<Integer, Integer> getAllViolationsWithLength(final Path path) throws CheckerException {
        try (Stream<String> lines = Files.lines(path)) {
            return getAllViolationWithLength(lines);
        }
        catch (IOException | UncheckedIOException e) {
            throw new CheckerException("Cannot read file " + path, e);
        }
    }

    private SortedMap<Integer, Integer> getAllViolationWithLength(final Stream<String> fileLines) {
        final SortedMap<Integer, Integer> violationsWithLength = new TreeMap<>();
        final Iterator<String> lines = fileLines.sequential().iterator();
        int lineIdx = 1;

        while (lines.hasNext()) {
            final String line = lines.next();

            if (line.length() > maxLength) {
                violationsWithLength.put(lineIdx, line.length());
            }

            lineIdx += 1;
        }

        return violationsWithLength;
    }
}
