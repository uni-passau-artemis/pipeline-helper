package de.uni_passau.fim.se2.pipeline_helper.checkers;

import static com.google.common.truth.Truth.assertThat;
import static de.uni_passau.fim.se2.pipeline_helper.TestUtil.resource;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

class DejagnuLogCheckerTest {

    @Test
    void shouldRecogniseFailingTestRun() throws Exception {
        final DejagnuLogChecker checker = new DejagnuLogChecker(resource("dejagnu_logs/gcd2.log"), "gcd2");
        final CheckerResult result = checker.check();

        assertThat(result.getName()).isEqualTo("gcd2");
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo("""
            spawn java -cp ../target/classes gcd.Shell
            gcd> Running ./gcd.tests/public.exp ...

            FAIL: 4 5 (expected "gcd(4, 5) = 1")

            FAIL: 91 21 (expected "gcd(91, 21) = 7")

            FAIL: 960 18 (expected "gcd(960, 18) = 6")

            FAIL: 1023 1 (expected "gcd(1023, 1) = 1")

            FAIL: 9 0 (expected "gcd(9, 0) = 9")""");
    }

    @Test
    void shouldRecogniseSuccessfulTestRun() throws Exception {
        final DejagnuLogChecker checker = new DejagnuLogChecker(resource("dejagnu_logs/gcd.log"), "gcd");
        final CheckerResult result = checker.check();

        assertThat(result.getName()).isEqualTo("gcd");
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getMessage()).isEqualTo("""
            spawn java -cp ../target/classes gcd.Shell
            gcd> Running ./gcd.tests/public.exp ...
            4 5
            gcd(4, 5) = 1
            gcd> PASS: 4 5
            91 21
            gcd(91, 21) = 7
            gcd> PASS: 91 21
            960 18
            gcd(960, 18) = 6
            gcd> PASS: 960 18
            1023 1
            gcd(1023, 1) = 1
            gcd> PASS: 1023 1
            9 0
            gcd(9, 0) = 9
            gcd> PASS: 9 0
            9
            Error! Input is unequal to two numbers.
            gcd> PASS: 9
            testcase ./gcd.tests/public.exp completed in 0 seconds""");
    }

    @Test
    void shouldGenerateAReplacementResultIfLogFileNotFound() throws CheckerException {
        final DejagnuLogChecker checker = new DejagnuLogChecker(Path.of("not_existing"), "gcd");
        final CheckerResult result = checker.check();

        assertThat(result.getName()).isEqualTo("gcd");
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo(
            "Could not generate a report for this test. Maybe the test ran longer than expected and got aborted."
        );
    }

    @Test
    void shouldAddATimeoutMessageIfTerminated() throws Exception {
        final DejagnuLogChecker checker = new DejagnuLogChecker(resource("dejagnu_logs/gcd_timeout.log"), "gcd");
        final CheckerResult result = checker.check();

        assertThat(result.getName()).isEqualTo("gcd");
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo(
            """
                The test took too long and got aborted. Check your code for possibly infinite loops or other long-running sections.
                spawn java -cp ../target/classes gcd.Shell
                gcd> Running ./gcd.tests/public.exp ...
                got a TERM signal, terminated"""
        );
    }
}
