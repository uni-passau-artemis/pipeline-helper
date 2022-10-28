package de.uni_passau.fim.se2.pipeline_helper.checkers;

import de.uni_passau.fim.se2.pipeline_helper.model.Checker;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

/**
 * Wraps any message into a {@link CheckerResult}
 */
public class SimpleMessageChecker implements Checker {

    private final String testCaseName;
    private final boolean successful;
    private final String message;

    public SimpleMessageChecker(final String testCaseName, final boolean successful, final String message) {
        this.testCaseName = testCaseName;
        this.successful = successful;
        this.message = message;
    }

    @Override
    public CheckerResult check() throws CheckerException {
        return new CheckerResult(testCaseName, successful, message);
    }
}
