package de.uni_passau.fim.se2.pipeline_helper.checkers;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

class SimpleMessageCheckerTest {

    @Test
    void shouldProduceAValidCheckerResult() throws CheckerException {
        final CheckerResult result = new SimpleMessageChecker("someName", true, "some msg").check();
        assertThat(result.getName()).isEqualTo("someName");
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getMessage()).isEqualTo("some msg");
    }

    @Test
    void shouldNotAllowEmptyMessageForFailedTests() {
        final CheckerException e = assertThrows(
            CheckerException.class, () -> new SimpleMessageChecker("someName", false, null).check()
        );
        assertThat(e).hasMessageThat().contains("Feedback for non-successful checkes cannot be null or empty!");

        final CheckerException e2 = assertThrows(
            CheckerException.class, () -> new SimpleMessageChecker("someName", false, "\t\n   ").check()
        );
        assertThat(e2).hasMessageThat().contains("Feedback for non-successful checkes cannot be null or empty!");
    }

    @Test
    void shouldNotAllowEmptyTestNames() {
        final CheckerException e1 = assertThrows(
            CheckerException.class, () -> new SimpleMessageChecker("", false, "asd").check()
        );
        assertThat(e1).hasMessageThat().contains("CheckerName cannot be null or empty!");

        final CheckerException e2 = assertThrows(
            CheckerException.class, () -> new SimpleMessageChecker("  \t ", true, "x").check()
        );
        assertThat(e2).hasMessageThat().contains("CheckerName cannot be null or empty!");

        final CheckerException e3 = assertThrows(
            CheckerException.class, () -> new SimpleMessageChecker(null, false, "d8").check()
        );
        assertThat(e3).hasMessageThat().contains("CheckerName cannot be null or empty!");
    }
}
