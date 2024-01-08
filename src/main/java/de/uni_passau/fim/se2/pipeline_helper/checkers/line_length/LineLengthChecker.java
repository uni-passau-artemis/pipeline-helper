// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers.line_length;

import java.io.IOException;
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

    public LineLengthChecker(final Stream<Path> files, final int maxLength, final Path directory) {
        this.files = files;
        this.maxLength = maxLength;
        this.directory = directory;
    }

    @Override
    public CheckerResult check() throws CheckerException {
        List<FileLineLengthViolations> violations = new ArrayList<>();
        for (Iterator<Path> it = files.iterator(); it.hasNext();) {
            final Path p = it.next();
            final List<Integer> linesWithViolations = getAllLineIndicesWithViolations(p);
            if (!linesWithViolations.isEmpty()) {
                violations.add(new FileLineLengthViolations(directory.relativize(p), linesWithViolations.size(), linesWithViolations));
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
    
    private List<Integer> getAllLineIndicesWithViolations(Path path) throws CheckerException {
        List<Integer> lineNumbers = new ArrayList<>();
        try {
            final List<String> lines = Files.readAllLines(path);
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).length() > maxLength) {
                    // add one, since lines are usually enumerated starting at 1
                    lineNumbers.add(i + 1);
                }
            }
        } catch (IOException e) {
            throw new CheckerException("Cannot read file " + path, e);
        }
        return lineNumbers;
    }
}
