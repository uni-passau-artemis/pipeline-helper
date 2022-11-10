// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import de.uni_passau.fim.se2.pipeline_helper.Main;
import picocli.CommandLine;

public abstract class IOTest {

    protected CommandLine commandLine;

    private PrintStream originalOutput;
    private InputStream originalInput;
    protected ByteArrayOutputStream newOutput;

    protected static final String NEWLINE = System.lineSeparator();

    @BeforeEach
    void setupStdinStdout() {
        originalOutput = System.out;
        newOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(newOutput, true, StandardCharsets.UTF_8));

        originalInput = System.in;
    }

    @BeforeEach
    void setupCommandLine() {
        commandLine = new CommandLine(new Main());
    }

    @AfterEach
    void resetStdinStdout() {
        System.setOut(originalOutput);
        System.setIn(originalInput);
    }

    protected void setInput(final String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Asserts console output.
     * <p>
     * Adds the final newline to the output before checking.
     */
    protected void assertOutput(final String output) {
        assertThat(getOutput()).isEqualTo(normalise(output) + NEWLINE);
    }

    protected void assertOutputContains(final CharSequence... values) {
        final String output = getOutput();
        assertAll(Arrays.stream(values).map(value -> () -> {
            assertThat(output).contains(value);
        }));
    }

    protected void assertOutputLineCount(final int lineCount) {
        final String output = getOutput();
        assertThat(output.lines().count()).isEqualTo(lineCount);
    }

    protected void assertEmptyOutput() {
        final String output = getOutput();
        assertThat(output.isBlank()).isTrue();
    }

    protected String getOutput() {
        return newOutput.toString(StandardCharsets.UTF_8);
    }

    private static String normalise(final String s) {
        return s.replace("\n", NEWLINE);
    }
}
