// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

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
        final String packagePath = "de/uni_passau/fim/se2/pipeline_helper/checkers/main_method_examples/";

        assertAll(
            () -> assertThat(result.isSuccessful()).isFalse(),
            // Check if message contains only valid classes and the correct number of them.
            () -> assertThat(result.getMessage())
                .matches(
                    "Found multiple main methods:" +
                        "(\\n%s[a-zA-Z$.]+( \\(x[0-9]+\\))?)+".formatted(packagePath.replace("/", "\\."))
                ),
            // Check for number of main methods for classes with more than one valid main method
            () -> assertThat(result.getMessage())
                .contains(packagePath.replace('/', '.') + "invalid.MultipleValidMethods (x2)\n")
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

}
