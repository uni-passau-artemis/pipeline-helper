// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers;

import static com.google.common.truth.Truth.assertThat;
import static de.uni_passau.fim.se2.pipeline_helper.TestUtil.resource;

import de.uni_passau.fim.se2.pipeline_helper.checkers.line_length.LineLengthChecker;
import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.pipeline_helper.helpers.FilteredFilesStream;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

class LineLengthCheckerTest {

    @Test
    void checkFailed() throws Exception {
        final LineLengthChecker checker = new LineLengthChecker(
            FilteredFilesStream.files(resource("line_length_checker_demo_files/"), "java"), 80
        );
        final CheckerResult result = checker.check();
        assertThat(result.getName()).contains("LineLengthChecker");
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).contains(
                "target/test-classes/line_length_checker_demo_files/invalid/InvalidFile.java, on 2 lines: 7 & 10");
        assertThat(result.getMessage()).contains(
                "target/test-classes/line_length_checker_demo_files/also_invalid/InvalidFile.java, on 2 lines: 7 & 10");
    }

    @Test
    void checkSuccess() throws Exception {
        final LineLengthChecker checker = new LineLengthChecker(
            FilteredFilesStream.files(resource("line_length_checker_demo_files/valid/"), "java"),
            80
        );
        final CheckerResult result = checker.check();
        final CheckerResult expectedResult = new CheckerResult("LineLengthChecker", true);
        assertThat(result).isEqualTo(expectedResult);
    }
}
