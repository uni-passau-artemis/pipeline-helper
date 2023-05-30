// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.pipeline_helper.model.Checker;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

/**
 * Checks that there is exactly one file with a Java main method
 */
public class MainMethodChecker implements Checker {

    private static final String CHECKER_NAME = "MainMethodChecker";

    /**
     * Matches Java main methods.
     * <p>
     * Either {@code public static void main(String[] args) {}} or {@code public
     * static void main(String... args) {}} with arbitrary whitepspace between
     * lexemes.
     * <p>
     * It matches, when the function throws exceptions. It does not match, when it
     * is inside a comment.
     * <p>
     * Might yield wrong results when a matching String literal is in the code.
     */
    private static final Pattern MAIN_METHOD = Pattern.compile(
        "^\\s*(?!//|\\*).*public\\s+static\\s+void\\s+main\\((java\\.lang\\.)?String\\s*(\\[]|\\.\\.\\.)\\s+[a-zA-Z]\\w*\\)\\s*(throws\\s+.+)?\\{$",
        Pattern.MULTILINE
    );

    private final Stream<Path> files;

    public MainMethodChecker(final Stream<Path> files) {
        this.files = files;
    }

    boolean hasMainMethod(final String fileContent) {
        final Matcher m = MAIN_METHOD.matcher(fileContent);
        return m.find();
    }

    @Override
    public CheckerResult check() throws CheckerException {
        final Set<Path> filesWithMainMethods = new HashSet<>();

        for (Iterator<Path> it = files.iterator(); it.hasNext();) {
            final Path p = it.next();
            try {
                if (hasMainMethod(Files.readString(p))) {
                    filesWithMainMethods.add(p);
                }
            }
            catch (IOException e) {
                throw new CheckerException("Cannot read file " + p, e);
            }
        }

        final CheckerResult result;

        if (filesWithMainMethods.isEmpty()) {
            result = new CheckerResult(CHECKER_NAME, false, "Could not find a main method!");
        }
        else if (filesWithMainMethods.size() == 1) {
            final String mainClass = asClassPath(filesWithMainMethods.stream().findFirst().orElseThrow());
            System.out.println(mainClass);
            result = new CheckerResult(
                CHECKER_NAME,
                true,
                String.format("Found main method in %s", mainClass)
            );
        }
        else {
            final String filesWithMainMethod = filesWithMainMethods.stream()
                .map(this::asClassPath)
                .collect(Collectors.joining("\n"));
            result = new CheckerResult(
                CHECKER_NAME,
                false,
                String.format("Found multiple files with main methods:%n%s", filesWithMainMethod)
            );
        }

        return result;
    }

    /**
     * Turns the given {@link Path} into a fully qualified class name.
     * <p>
     * Trims all leading directories up to and including {@code src}.
     * Assumes that all folders below that are java packages.
     * Removes the {@code .java} file extension.
     * <p>
     * Example:
     * {@code assignment/src/gcd/Shell.java}
     * will be turned into
     * {@code gcd.Shell}
     *
     * @param mainClassFile a Java-file.
     * @return the fully qualified name of the class defined by this file.
     */
    private String asClassPath(final Path mainClassFile) {
        return mainClassFile.toAbsolutePath()
            .toString()
            .replaceAll("^.*/src/", "")
            .replaceAll("\\.java$", "")
            .replace('/', '.');
    }
}
