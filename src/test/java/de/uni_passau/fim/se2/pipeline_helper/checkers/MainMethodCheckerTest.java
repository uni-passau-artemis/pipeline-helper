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
        final String packagePath = "de/uni_passau/fim/se2/pipeline_helper/checkers/mainMethodExamples/valid/";
        final int validCount = Objects.requireNonNull(new File("src/test/java/" + packagePath).list()).length
                - 1; // Without package-info.java

        assertAll(
            () -> assertThat(result.isSuccessful()).isFalse(),
            // Check if message contains only valid classes and the correct number of them.
            () -> assertThat(result.getMessage())
                .matches(
                    "Found multiple main methods:" +
                        "(\\n%s[a-zA-Z]+){%d}".formatted(packagePath.replace("/", "\\."), validCount)
                )
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
