// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import de.uni_passau.fim.se2.pipeline_helper.model.Checker;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

/**
 * Creates a {@link CheckerResult} from a Dejagnu log file.
 * <p>
 * Trims down the log file to only relevant parts. Detects failed tests and sets the success status in the result
 * accordingly.
 */
public class DejagnuLogChecker implements Checker {

    private static final String SUMMARY_START_LOG = "Summary ===";
    private static final String TERMINATED_LOG = "got a TERM signal, terminated";

    private static final String FILE_NOT_FOUND_MESSAGE = "Could not generate a report for this test. Maybe the test ran longer than expected and got aborted.";
    private static final String TIMEOUT_MESSAGE = "The test took too long and got aborted. Check your code for possibly infinite loops or other long-running sections.";

    private final String testName;
    private final Path logFile;

    public DejagnuLogChecker(Path logFile, String testName) {
        this.logFile = logFile;
        this.testName = testName;
    }

    @Override
    public CheckerResult check() throws CheckerException {
        if (!Files.exists(logFile)) {
            return generateResultMissingFile();
        }

        final List<String> fileContent = readLogLines();

        final DejagnuLog extractedLog;
        try {
            extractedLog = extractLogFromFile(fileContent);
        }
        catch (IllegalArgumentException invalidLogException) {
            throw new CheckerException(String.format("Invalid Dejagnu log file: %s", logFile));
        }

        final String message = buildCheckerMessage(extractedLog.lines(), extractedLog.hasBeenTerminated());
        final boolean isSuccessful = !extractedLog.hasFailures() && !extractedLog.hasBeenTerminated();

        return new CheckerResult(testName, isSuccessful, message);
    }

    private List<String> readLogLines() throws CheckerException {
        try {
            return Files.readAllLines(logFile);
        }
        catch (IOException e) {
            throw new CheckerException("Cannot read Dejagnu logfile", e);
        }
    }

    private record DejagnuLog(List<String> lines, boolean hasFailures, boolean hasBeenTerminated) {
    }

    private DejagnuLog extractLogFromFile(final List<String> lines) throws IllegalArgumentException {
        int firstLine = -1;
        int lastLine = -1;
        boolean foundFailures = false;
        boolean hasBeenTerminated = false;

        for (int i = 0; i < lines.size(); ++i) {
            final String line = lines.get(i);
            // log starts with first program start
            if (line.startsWith("spawn ") && firstLine == -1) {
                firstLine = i;
            }
            else if (line.endsWith(SUMMARY_START_LOG)) {
                lastLine = i;
            }
            else if (line.contains(TERMINATED_LOG)) {
                hasBeenTerminated = true;
            }
            else if (line.startsWith("# of unexpected failures") || line.startsWith("FAIL:")) {
                foundFailures = true;
            }
        }

        if (firstLine == -1 || lastLine == -1) {
            throw new IllegalArgumentException(String.format("Invalid Dejagnu log file: %s", logFile));
        }

        return new DejagnuLog(lines.subList(firstLine, lastLine), foundFailures, hasBeenTerminated);
    }

    private String buildCheckerMessage(final List<String> log, boolean hasBeenTerminated) {
        String message = "";
        if (hasBeenTerminated) {
            message += TIMEOUT_MESSAGE + "\n";
        }
        message += log.stream().map(this::processLogLine).collect(Collectors.joining("\n"));

        return message.trim();
    }

    private String processLogLine(final String logLine) {
        String processed = logLine;

        // Artemis postprocessing would otherwise remove stacktrace and
        // everything following it by matching the start of the line
        if (logLine.startsWith("\tat ")) {
            processed = processed.replaceFirst("\tat ", "    at ");
        }

        return processed;
    }

    private CheckerResult generateResultMissingFile() throws CheckerException {
        return new CheckerResult(testName, false, FILE_NOT_FOUND_MESSAGE);
    }
}
