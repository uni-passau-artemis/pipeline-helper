package de.uni_passau.fim.se2.pipeline_helper.model;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CheckerResultTest {

    @Test
    void shouldNotAllowEmptyMessageForFailedTests() {
        final CheckerException e = assertThrows(CheckerException.class, () -> new CheckerResult("someName", false));
        assertThat(e).hasMessageThat().contains("Feedback for non-successful checkes cannot be null or empty!");

        final CheckerException e2 = assertThrows(
            CheckerException.class, () -> new CheckerResult("someName", false, "\t\n   ")
        );
        assertThat(e2).hasMessageThat().contains("Feedback for non-successful checkes cannot be null or empty!");
    }

    @Test
    void shouldNotAllowEmptyTestNames() {
        final CheckerException e1 = assertThrows(CheckerException.class, () -> new CheckerResult("", false));
        assertThat(e1).hasMessageThat().contains("CheckerName cannot be null or empty!");

        final CheckerException e2 = assertThrows(CheckerException.class, () -> new CheckerResult("  \t ", true));
        assertThat(e2).hasMessageThat().contains("CheckerName cannot be null or empty!");

        final CheckerException e3 = assertThrows(CheckerException.class, () -> new CheckerResult(null, false));
        assertThat(e3).hasMessageThat().contains("CheckerName cannot be null or empty!");
    }
}
