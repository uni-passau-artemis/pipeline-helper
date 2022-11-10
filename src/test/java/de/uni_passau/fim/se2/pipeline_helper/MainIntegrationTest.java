// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.uni_passau.fim.se2.pipeline_helper.util.IOTest;

class MainIntegrationTest extends IOTest {

    private static final String LINE_LENGTH_CHECKER_RESULT_FILE = "TEST-LineLengthChecker.json";

    @Test
    void testLineLengthCheckerThreeArgs(@TempDir Path outputDir) {
        commandLine.execute("-o", outputDir.toString(), "-l", "80", "src/", "java");
        assertOutputContains("Successfully produced 1 checker results!");
        assertCheckerResultCreated(outputDir.resolve(LINE_LENGTH_CHECKER_RESULT_FILE));
    }

    @Test
    void testLineLengthCheckerTwoArgs(@TempDir Path outputDir) {
        commandLine.execute("-o", outputDir.toString(), "-l", "80", "src/");
        assertOutputContains("Successfully produced 1 checker results!");
        assertCheckerResultCreated(outputDir.resolve(LINE_LENGTH_CHECKER_RESULT_FILE));
    }

    @Test
    void testCustomFeedbackCreator(@TempDir Path outputDir) throws IOException {
        commandLine.execute("-o", outputDir.toString(), "-s", "simpleMessage", "true", "'Actual message'");
        assertOutputContains("Successfully produced 1 checker results!");

        final Path expectedOutputPath = outputDir.resolve(outputDir.resolve("TEST-simpleMessage.json"));
        assertCheckerResultCreated(expectedOutputPath);
        assertCheckerResultContains(expectedOutputPath, """
            {"name":"simpleMessage","successful":true,"message":"\\u0027Actual message\\u0027"}
            """.trim());
    }

    private void assertCheckerResultCreated(final Path path) {
        final File output = path.toFile();
        assertThat(output.exists()).isTrue();
        assertThat(output.isFile()).isTrue();
    }

    private void assertCheckerResultContains(final Path checkerResultFile, String... content) throws IOException {
        final String checkerResult = Files.readString(checkerResultFile);
        assertAll(Arrays.stream(content).map(value -> () -> assertThat(checkerResult).contains(value)));
    }
}
