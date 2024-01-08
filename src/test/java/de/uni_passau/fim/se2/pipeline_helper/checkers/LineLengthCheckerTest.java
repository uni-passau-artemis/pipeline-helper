// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers;

import static com.google.common.truth.Truth.assertThat;
import static de.uni_passau.fim.se2.pipeline_helper.TestUtil.resource;

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
    void checkFailed() throws Exception {
        final LineLengthChecker checker = new LineLengthChecker(
                FilteredFilesStream.files(dir, "java"), 80, dir);
        final CheckerResult result = checker.check();
        assertThat(result.getName()).contains("LineLengthChecker");
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).contains("invalid/InvalidFile.java: 2 lines");
        assertThat(result.getMessage()).contains("also_invalid/InvalidFile.java: 2 lines");
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
