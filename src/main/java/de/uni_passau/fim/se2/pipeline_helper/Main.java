// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.pipeline_helper.checkers.*;
import de.uni_passau.fim.se2.pipeline_helper.checkers.line_length.LineLengthChecker;
import de.uni_passau.fim.se2.pipeline_helper.helpers.CheckerResultWriter;
import de.uni_passau.fim.se2.pipeline_helper.helpers.FilteredFilesStream;
import de.uni_passau.fim.se2.pipeline_helper.model.Checker;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;
import picocli.CommandLine;
import picocli.CommandLine.*;

@Command(
    name = "pipeline-helper",
    version = "2.0.1",
    mixinStandardHelpOptions = true,
    subcommandsRepeatable = true,
    showDefaultValues = true,
    subcommands = {
        Main.DejagnuCheckerSubcommand.class,
        Main.FileExistsCheckerSubcommand.class,
        Main.LineLengthCheckerSubcommand.class,
        Main.MainMethodCheckerSubcommand.class,
        Main.SimpleMessageCheckerSubcommand.class,
    },
    description = """
        Various checkers formerly used in Praktomat ported for integration with Artemis

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
        description = "The directory where the results will be placed."
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

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println("""
            Use one or more subcommands that specify checkers.
            Run with '--help' for details.""");
    }

    @Command(
        mixinStandardHelpOptions = true,
        showDefaultValues = true
    )
    private abstract static class CheckerSubcommand implements Callable<Integer> {

        @Spec
        Model.CommandSpec spec;

        @ParentCommand
        Main parent;

        protected abstract Checker buildChecker() throws Exception;

        protected void validateParams() throws ParameterException {
            // intentionally empty, subcommands can optionally override with custom logic
        }

        @Override
        public Integer call() throws Exception {
            validateParams();

            final Checker checker = buildChecker();
            final CheckerResult result = checker.check();
            CheckerResultWriter.writeFeedback(parent.outputDirectory, result);

            System.out.println("Successfully produced a checker result.");

            return 0;
        }
    }

    @Command(
        name = "dejagnu",
        description = "Parses a Dejagnu log file and converts it into a result."
    )
    static class DejagnuCheckerSubcommand extends CheckerSubcommand {

        @Option(
            names = { "-l", "--log" },
            description = "The log file of the Dejagnu run.",
            required = true
        )
        Path logFile;

        @Option(
            names = { "-n", "--name" },
            description = "The unique name for the produced result.",
            required = true
        )
        String testName;

        @Override
        protected Checker buildChecker() throws Exception {
            return new DejagnuLogChecker(logFile, testName);
        }
    }

    @Command(
        name = "main-method",
        description = "Searches for Java main methods."
    )
    static class MainMethodCheckerSubcommand extends CheckerSubcommand {

        @Option(
            names = { "-s", "--search-path" },
            description = "A directory that contains .class files.",
            required = true
        )
        Path searchPath;

        @Override
        protected Checker buildChecker() {
            return new MainMethodChecker(searchPath);
        }
    }

    @Command(
        name = "file-exists",
        description = "Checks that the files exist and are not empty."
    )
    static class FileExistsCheckerSubcommand extends CheckerSubcommand {

        @Parameters(description = "Files that should exist an not be empty.")
        List<Path> files;

        @Override
        protected Checker buildChecker() {
            return new FileExistsChecker(files);
        }
    }

    @Command(
        name = "line-length",
        description = "Checks that a set of files only contain lines shorter that a threshold."
    )
    static class LineLengthCheckerSubcommand extends CheckerSubcommand {

        @Option(
            names = { "-l", "--length" },
            description = "Checks that all lines in the given files only contain lines that are not longer than this length.",
            defaultValue = "80"
        )
        int lineLength;

        @Option(
            names = { "-e", "--ext" },
            description = "Only checks files with the given file extension.",
            defaultValue = "java"
        )
        String fileExtension;

        @Option(
            names = { "-s", "--search-path" },
            description = "Directory that should be checked recursively.",
            required = true
        )
        Path directory;

        @Override
        protected Checker buildChecker() throws Exception {
            final Stream<Path> files = FilteredFilesStream.files(directory, fileExtension);
            return new LineLengthChecker(files, lineLength, directory);
        }
    }

    @Command(
        name = "message",
        description = "Produces a custom test result message in the required format for Artemis."
    )
    static class SimpleMessageCheckerSubcommand extends CheckerSubcommand {

        @Option(
            names = { "-n", "--name" },
            description = "A unique name for the result.",
            required = true
        )
        String name;

        @Option(
            names = { "-s", "--successful" },
            defaultValue = "false"
        )
        boolean successful;

        @Option(
            names = { "-m", "--message" },
            description = "The message shown to the student"
        )
        String message;

        @Override
        protected void validateParams() throws ParameterException {
            if (!successful && message == null) {
                throw new ParameterException(
                    spec.commandLine(),
                    "A message is required if the checker result is not successful."
                );
            }
        }

        @Override
        protected Checker buildChecker() {
            return new SimpleMessageChecker(name, successful, message);
        }
    }
}
