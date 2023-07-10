// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

class MainMethodCheckerTest {

    @Test
    void checkerSingleMainMethod() throws CheckerException {
        final MainMethodChecker checker = new MainMethodChecker(Path.of("target/classes/"));
        final CheckerResult result = checker.check();

        final CheckerResult expectedResult = new CheckerResult(
            "MainMethodChecker", true,
            "Found main method in de.uni_passau.fim.se2.pipeline_helper.Main"
        );
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void checkerMultipleMainMethods() throws CheckerException {
        final MainMethodChecker checker = new MainMethodChecker(Path.of("target/test-classes/"));
        final CheckerResult result = checker.check();

        assertAll(
            () -> assertThat(result.isSuccessful()).isFalse(),
            () -> assertThat(result.getMessage()).contains("Found multiple main methods:"),
            () -> assertThat(result.getMessage())
                .contains("de.uni_passau.fim.se2.pipeline_helper.checkers.MainMethodCheckerTest"),
            () -> assertThat(result.getMessage())
                .contains("de.uni_passau.fim.se2.pipeline_helper.checkers.MainMethodCheckerTest$InnerClassMain")
        );
    }

    @Test
    void checkerNoMainMethods() throws CheckerException {
        final MainMethodChecker checker = new MainMethodChecker(Path.of("target/"));
        final CheckerResult result = checker.check();

        final CheckerResult expectedResult = new CheckerResult(
            "MainMethodChecker", false,
            "Could not find a main method!"
        );
        assertThat(result).isEqualTo(expectedResult);
    }

    public static void main(String[] args) {
    }

    public static class InnerClassMain {

        public static void main(String... args) {
        }
    }

    public static class InvalidModifiers {

        void main(String[] args) {
        }
    }

    public static class InvalidModifiers2 {

        public void main(String[] args) {
        }
    }

    public static class InvalidModifiers3 {

        static void main(String[] args) {
        }
    }

    @SuppressWarnings("unused")
    public static class InvalidReturnType {

        public static int main(String[] args) {
            return 0;
        }
    }
}
