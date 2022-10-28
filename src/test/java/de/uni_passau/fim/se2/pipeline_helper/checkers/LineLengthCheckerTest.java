package de.uni_passau.fim.se2.pipeline_helper.checkers;

import static com.google.common.truth.Truth.assertThat;
import static de.uni_passau.fim.se2.pipeline_helper.TestUtil.resource;

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

        final CheckerResult expectedResult = new CheckerResult("LineLengthChecker", false, """
            Found files with lines longer than 80 characters:
            InvalidFile.java: 2 lines""");
        assertThat(result).isEqualTo(expectedResult);
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
