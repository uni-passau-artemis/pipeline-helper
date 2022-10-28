package de.uni_passau.fim.se2.pipeline_helper.checkers;

import de.uni_passau.fim.se2.pipeline_helper.model.Checker;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Checks that all lines in all files have a maximum length.
 */
public class LineLengthChecker implements Checker {
    private static final String CHECKER_NAME = "LineLengthChecker";

    private final Stream<Path> files;
    private final int maxLength;

    public LineLengthChecker(final Stream<Path> files, final int maxLength) {
        this.files = files;
        this.maxLength = maxLength;
    }

    @Override
    public CheckerResult check() throws CheckerException {
        final Map<String, Integer> violations = new HashMap<>();

        for (Iterator<Path> it = files.iterator(); it.hasNext(); ) {
            final Path p = it.next();
            try {
                final int count = (int) Files.readAllLines(p).stream().filter(l -> l.length() > maxLength).count();
                if (count > 0) {
                    violations.put(p.getFileName().toString(), count);
                }
            } catch (IOException e) {
                throw new CheckerException("Cannot read file " + p, e);
            }
        }

        if (violations.isEmpty()) {
            return new CheckerResult(CHECKER_NAME, true);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Found files with lines longer than %d characters:%n", maxLength));
        for (var entry : violations.entrySet()) {
            sb.append(String.format("%s: %d lines%n", entry.getKey(), entry.getValue()));
        }

        return new CheckerResult(CHECKER_NAME, false, sb.toString().trim());
    }
}
