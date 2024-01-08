// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers;

import static com.google.common.truth.Truth.assertThat;
import static de.uni_passau.fim.se2.pipeline_helper.TestUtil.resource;

import de.uni_passau.fim.se2.pipeline_helper.checkers.line_length.LineLengthChecker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.pipeline_helper.helpers.FilteredFilesStream;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

import java.net.URISyntaxException;
import java.nio.file.Path;

class LineLengthCheckerTest {

    private static Path dir;
    
    @BeforeAll
    static void init() throws URISyntaxException {
        dir = resource("line_length_checker_demo_files/");
    }
    @Test
    void checkFailedSingleViolation() throws Exception {
        final LineLengthChecker checker = 
                new LineLengthChecker(FilteredFilesStream.files(dir, "java"), 80, dir);
        final CheckerResult result = checker.check();
        assertThat(result.getName()).contains("LineLengthChecker");
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).contains(
                """
                invalid/InvalidFileSingleViolation.java, on 1 line:
                    -> line 7, length 82"""
        );
    }

    @Test
    void checkFailedTwoViolationsDifferentFiles() throws Exception {
        final LineLengthChecker checker = 
                new LineLengthChecker(FilteredFilesStream.files(dir, "java"), 80, dir);
        final CheckerResult result = checker.check();
        assertThat(result.getName()).contains("LineLengthChecker");
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).contains(
                """
                invalid/InvalidFile.java, on 2 lines:
                    -> line 7, length 82
                    -> line 10, length 99"""
        );
        assertThat(result.getMessage()).contains(
                """
                also_invalid/InvalidFile.java, on 2 lines:
                    -> line 7, length 82
                    -> line 10, length 99"""
        );
    }

    @Test
    void checkFailedMoreThanTwoViolations() throws Exception {
        final LineLengthChecker checker = 
                new LineLengthChecker(FilteredFilesStream.files(dir, "java"), 80, dir);
        final CheckerResult result = checker.check();
        assertThat(result.getName()).contains("LineLengthChecker");
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).contains(
                """
                invalid/InvalidFileFourViolations.java, on 4 lines:
                    -> line 7, length 82
                    -> line 8, length 82
                    -> line 9, length 82
                    -> line 10, length 82"""
        );
    }

    @Test
    void checkSuccess() throws Exception {
        Path validDir = dir.resolve("valid/");
        final LineLengthChecker checker = new LineLengthChecker(
                FilteredFilesStream.files(validDir, "java"), 80, validDir);
        final CheckerResult result = checker.check();
        final CheckerResult expectedResult = new CheckerResult("LineLengthChecker", true);
        assertThat(result).isEqualTo(expectedResult);
    }
}
