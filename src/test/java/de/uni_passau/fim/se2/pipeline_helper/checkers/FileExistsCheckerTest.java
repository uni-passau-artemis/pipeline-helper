// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers;

import static com.google.common.truth.Truth.assertThat;
import static de.uni_passau.fim.se2.pipeline_helper.TestUtil.resource;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.pipeline_helper.checkers.file_exists.FileExistsChecker;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

class FileExistsCheckerTest {

    private static final Path TEST_RESOURCES_PATH = Path.of("src/test/resources");

    @Test
    void shouldRecogniseExistingFiles() throws Exception {
        final FileExistsChecker checker = new FileExistsChecker(
            List.of(
                resource("dejagnu_logs/gcd.log"),
                resource("dejagnu_logs/gcd2.log")
            )
        );

        final CheckerResult result = checker.check();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getMessage()).isNull();
    }

    @Test
    void shouldRecogniseEmptyFiles() throws Exception {
        final FileExistsChecker checker = new FileExistsChecker(
            List.of(
                resource("empty.txt"),
                resource("dejagnu_logs/gcd.log"),
                resource("dejagnu_logs/gcd2.log")
            )
        );

        final CheckerResult result = checker.check();
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo("""
            Empty files:
            empty.txt""");
    }

    @Test
    void shouldRecogniseMissingFiles() throws Exception {
        final FileExistsChecker checker = new FileExistsChecker(
            List.of(
                resource("dejagnu_logs/gcd.log"),
                resource("dejagnu_logs/gcd2.log"),
                Path.of("non-existing-file.txt")
            )
        );

        final CheckerResult result = checker.check();
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo("""
            Missing files:
            non-existing-file.txt""");
    }

    @Test
    void shouldRecogniseNonReadableFiles() throws Exception {
        final FileExistsChecker checker = new FileExistsChecker(
            List.of(
                resource("line_length_checker_demo_files/illegal_byte_sequence/InvalidByteSequence.java")
            )
        );

        final CheckerResult result = checker.check();
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo("""
            Non-readable files:
            InvalidByteSequence.java""");
    }

    @Test
    void shouldNotConsiderAnEmptyDirectoryAFile() throws Exception {
        final FileExistsChecker checker = new FileExistsChecker(
            List.of(TEST_RESOURCES_PATH.resolve("Tests.txt"))
        );
        final CheckerResult result = checker.check();
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo("""
            Missing files:
            Tests.txt""");
    }

    @Test
    void shouldRecogniseEmptyAndMissingAndNonReadableFiles() throws Exception {
        final FileExistsChecker checker = new FileExistsChecker(
            List.of(
                resource("empty.txt"),
                resource("dejagnu_logs/gcd.log"),
                resource("dejagnu_logs/gcd2.log"),
                resource("line_length_checker_demo_files/illegal_byte_sequence/InvalidByteSequence.java"),
                Path.of("non-existing-file.txt")
            )
        );

        final CheckerResult result = checker.check();
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo("""
            Missing files:
            non-existing-file.txt

            Empty files:
            empty.txt

            Non-readable files:
            InvalidByteSequence.java""");
    }
}
