// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;

import de.uni_passau.fim.se2.pipeline_helper.checkers.*;
import de.uni_passau.fim.se2.pipeline_helper.helpers.CheckerResultWriter;
import de.uni_passau.fim.se2.pipeline_helper.helpers.FilteredFilesStream;
import de.uni_passau.fim.se2.pipeline_helper.model.Checker;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;
import picocli.CommandLine;
import picocli.CommandLine.*;

@Command(
    name = "pipeline-helper",
    version = "1.0.0",
    mixinStandardHelpOptions = true,
    description = """
        Various checkers formerly used in Prakomat ported for integration with Artemis

        Runs any combination of checkers to produce files readable by the Jenkins plugin
        which then in turn can send this information to Artemis.
        The DejagnuLogChecker and SimpleMessageWrapper can be specified multiple times
        with different testNames each time to allow e.g. to parse public, advanced, and
        secret tests in a single pass.

        Usage:"""
)
public final class Main implements Runnable {

    @Spec
    Model.CommandSpec spec;

    Path outputDirectory;

    /**
     * Sets the output directory according to the CLI parameter.
     *
     * @param value The output directory.
     */
    @Option(
        names = { "-o", "--output-directory" },
        defaultValue = "customFeedbacks",
        description = """
            The directory where the results will be placed.
            Default: customFeedbacks"""
    )
    public void setOutputDirectory(Path value) {
        if (Files.exists(value) && !Files.isDirectory(value)) {
            throw new ParameterException(spec.commandLine(), "The output directory already exists but is a file!");
        }

        if (!Files.exists(value)) {
            try {
                Files.createDirectories(value);
            }
            catch (IOException e) {
                throw new ParameterException(spec.commandLine(), "Cannot create output directory!");
            }
        }

        outputDirectory = value;
    }

    @Option(
        names = { "-m", "--main-method-checker" },
        paramLabel = "<searchDirectory>",
        description = """
            Runs the MainMethodChecker.
            Parameter: A directory in which a unique Java main method should be searched for."""
    )
    Path mainMethodCheckerInputDirectory;

    @Option(
        names = { "-e", "--file-exists-checker" },
        description = """
            Runs the FileExistsChecker.
            Parameters: A list of files that are checked to exist and be non-empty."""
    )
    List<Path> fileExistsCheckerFiles;

    @Option(
        names = { "-l", "--line-length-checker" },
        paramLabel = "<maxLength> <searchDirectory> <extension>",
        parameterConsumer = LineLengthCheckerArgsConverter.class,
        description = """
            Runs the LineLengthChecker.
            Parameters:
            - maxLength: maximum allowed line length
            - searchDir: directory to look for files in
            - extension: a file extension to filter the files on (optional, default: java)"""
    )
    LineLengthChecker lineLengthChecker;

    static class LineLengthCheckerArgsConverter implements IParameterConsumer {

        @Override
        public void consumeParameters(Stack<String> args, Model.ArgSpec argSpec, Model.CommandSpec commandSpec) {
            if (args.size() < 2) {
                throw new ParameterException(
                    commandSpec.commandLine(), "The line length checker needs at least two arguments!"
                );
            }

            int maxLineLength = Integer.parseInt(args.pop());
            Path fileSearchPath = Path.of(args.pop());

            String extension;
            if (args.empty()) {
                extension = "java";
            }
            else {
                extension = args.pop();
            }

            try {
                argSpec.setValue(
                    new LineLengthChecker(FilteredFilesStream.files(fileSearchPath, extension), maxLineLength)
                );
            }
            catch (IOException e) {
                System.err.printf("Cannot access file needed for LineLengthChecker: %s\n", e.getMessage());
                System.exit(1);
            }
        }
    }

    @Option(
        names = { "-d", "--dejagnu-log-checker" },
        paramLabel = "<testName> <logFile>",
        parameterConsumer = DejagnuLogCheckerArgsConverter.class,
        description = """
            Runs a DejagnuLogChecker.
            Parameters:
            - testName: name of the resulting test case
            - logFile: the Dejagnu log file to check"""
    )
    List<DejagnuLogChecker> dejagnuLogCheckerArgs = new ArrayList<>();

    static class DejagnuLogCheckerArgsConverter implements IParameterConsumer {

        @Override
        public void consumeParameters(Stack<String> args, Model.ArgSpec argSpec, Model.CommandSpec commandSpec) {
            if (args.size() < 2) {
                throw new ParameterException(
                    commandSpec.commandLine(), "The dejagnu log file converter needs two arguments!"
                );
            }
            String testName = args.pop();
            Path logFile = Path.of(args.pop());
            ((List<DejagnuLogChecker>) argSpec.getValue()).add(new DejagnuLogChecker(logFile, testName));
        }
    }

    @Option(
        names = { "-s", "--simple-message-wrapper" },
        paramLabel = "<testName> <successful> <message>",
        parameterConsumer = SimpleMessageArgsConverter.class,
        description = """
            Runs a SimpleMessageWrapping Checker.
            Parameters:
            - testName: name of the resulting test case
            - successful: if the test case should be marked as success or failed
            - message: the message shown to students"""
    )
    List<SimpleMessageChecker> simpleMessageCheckers = new ArrayList<>();

    static class SimpleMessageArgsConverter implements IParameterConsumer {

        @Override
        public void consumeParameters(Stack<String> args, Model.ArgSpec argSpec, Model.CommandSpec commandSpec) {
            if (args.size() < 3) {
                throw new ParameterException(
                    commandSpec.commandLine(), "The simple message converter needs three arguments!"
                );
            }
            ((List<SimpleMessageChecker>) argSpec.getValue())
                .add(new SimpleMessageChecker(args.pop(), Boolean.parseBoolean(args.pop()), args.pop()));
        }
    }

    private List<Checker> constructCheckerList() {
        final List<Checker> checkers = new ArrayList<>();

        checkers.addAll(dejagnuLogCheckerArgs);
        checkers.addAll(simpleMessageCheckers);

        if (mainMethodCheckerInputDirectory != null) {
            try {
                checkers.add(new MainMethodChecker(FilteredFilesStream.files(mainMethodCheckerInputDirectory, "java")));
            }
            catch (IOException e) {
                System.err.printf("Cannot access file needed for MainMethodChecker: %s\n", e.getMessage());
                System.exit(1);
            }
        }
        if (fileExistsCheckerFiles != null) {
            checkers.add(new FileExistsChecker(fileExistsCheckerFiles));
        }
        if (lineLengthChecker != null) {
            checkers.add(lineLengthChecker);
        }

        return checkers;
    }

    @Override
    public void run() {
        final List<Checker> checkers = constructCheckerList();

        final List<CheckerResult> results = checkers.stream()
            .map((Function<Checker, Optional<CheckerResult>>) checker -> {
                try {
                    return Optional.of(checker.check());
                }
                catch (CheckerException e) {
                    e.printStackTrace();
                    return Optional.empty();
                }
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        try {
            CheckerResultWriter.writeFeedback(outputDirectory, results);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.printf("Successfully produced %d checker results!%n", checkers.size());
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
