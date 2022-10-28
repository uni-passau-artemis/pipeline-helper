package de.uni_passau.fim.se2.pipeline_helper.checkers;

import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static de.uni_passau.fim.se2.pipeline_helper.TestUtil.resource;

class FileExistsCheckerTest {
    @Test
    void shouldRecogniseExistingFiles() throws Exception {
        final FileExistsChecker checker = new FileExistsChecker(List.of(
                resource("dejagnu_logs/gcd.log"),
                resource("dejagnu_logs/gcd2.log")
        ));

        final CheckerResult result = checker.check();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getMessage()).isNull();
    }

    @Test
    void shouldRecogniseEmptyFiles() throws Exception {
        final FileExistsChecker checker = new FileExistsChecker(List.of(
                resource("empty.txt"),
                resource("dejagnu_logs/gcd.log"),
                resource("dejagnu_logs/gcd2.log")
        ));

        final CheckerResult result = checker.check();
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo("""
                Empty or non-readable files:
                empty.txt""");
    }

    @Test
    void shouldRecogniseNonExistingFiles() throws Exception {
        final FileExistsChecker checker = new FileExistsChecker(List.of(
                resource("dejagnu_logs/gcd.log"),
                resource("dejagnu_logs/gcd2.log"),
                Path.of("non-existing-file.txt")
        ));

        final CheckerResult result = checker.check();
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo("""
                Missing files:
                non-existing-file.txt""");
    }

    @Test
    void shouldRecogniseEmptyAndNonExistingFiles() throws Exception {
        final FileExistsChecker checker = new FileExistsChecker(List.of(
                resource("empty.txt"),
                resource("dejagnu_logs/gcd.log"),
                resource("dejagnu_logs/gcd2.log"),
                Path.of("non-existing-file.txt")
        ));

        final CheckerResult result = checker.check();
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo("""
                Missing files:
                non-existing-file.txt

                Empty or non-readable files:
                empty.txt""");
    }
}
