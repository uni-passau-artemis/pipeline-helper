// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers;

import static com.google.common.truth.Truth.assertThat;
import static de.uni_passau.fim.se2.pipeline_helper.TestUtil.resource;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.pipeline_helper.checkers.line_length.LineLengthChecker;
import de.uni_passau.fim.se2.pipeline_helper.helpers.FilteredFilesStream;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

class LineLengthCheckerTest {

    private static Path dir;

    @BeforeAll
    static void init() throws URISyntaxException {
        dir = resource("line_length_checker_demo_files/");
    }

    @Test
    void checkFailedSingleViolation() throws Exception {
        final LineLengthChecker checker = new LineLengthChecker(
            dir, FilteredFilesStream.files(dir, "java"), 80
        );
        final CheckerResult result = checker.check();
        assertThat(result.getName()).contains("LineLengthChecker");
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).contains(
            withNormalisedNewline(
                """
                    invalid%sInvalidFileSingleViolation.java, on 1 line:
                        -> line 7, length 82""".formatted(File.separator)
            )
        );
    }

    @Test
    void checkFailedDifferentFiles() throws Exception {
        Path invalidDir = dir.resolve("invalid/");
        final LineLengthChecker checker = new LineLengthChecker(
            invalidDir, FilteredFilesStream.files(invalidDir, "java"), 80
        );
        final CheckerResult result = checker.check();
        assertThat(result.getName()).contains("LineLengthChecker");
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).contains(
            withNormalisedNewline(
                """
                    InvalidFile.java, on 2 lines:
                        -> line 7, length 82
                        -> line 10, length 99"""
            )
        );
        withNormalisedNewline(
                """
                    invalidFileSingleViolation.java, on 1 line:
                        -> line 7, length 82"""
        );
    }

    @Test
    void checkFailedIllegalByteSequence() throws Exception {
        Path illegalByteSeqDir = dir.resolve("illegal_byte_sequence/");
        final LineLengthChecker checker = new LineLengthChecker(
            illegalByteSeqDir, FilteredFilesStream.files(illegalByteSeqDir, "java"), 80
        );
        final CheckerResult result = checker.check();
        assertThat(result.getName()).contains("LineLengthChecker");
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Invalid byte sequence in file InvalidByteSequence.java");
    }

    @Test
    void checkSuccess() throws Exception {
        Path validDir = dir.resolve("valid/");
        final LineLengthChecker checker = new LineLengthChecker(
            validDir, FilteredFilesStream.files(validDir, "java"), 80
        );
        final CheckerResult result = checker.check();
        final CheckerResult expectedResult = new CheckerResult("LineLengthChecker", true);
        assertThat(result).isEqualTo(expectedResult);
    }

    private String withNormalisedNewline(final String message) {
        return message.replace("\n", System.lineSeparator());
    }
}
