package de.uni_passau.fim.se2.pipeline_helper.model;

import java.util.Objects;

public class CheckerResult {

    /**
     * Name which will be used in Artemis to identify this result.
     */
    private final String name;

    /**
     * If this checker should be marked as passed or failed.
     */
    private final boolean successful;

    /**
     * A message shown to the student.
     */
    private final String message;

    public CheckerResult(String name, boolean successful) throws CheckerException {
        this(name, successful, null);
    }

    public CheckerResult(String name, boolean successful, String message) throws CheckerException {
        if (name == null || name.isBlank()) {
            throw new CheckerException("CheckerName cannot be null or empty!");
        }
        if (!successful && (message == null || message.isBlank())) {
            throw new CheckerException("Feedback for non-successful checkes cannot be null or empty!");
        }

        this.name = name;
        this.successful = successful;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        else if (o instanceof CheckerResult result) {
            return successful == result.successful
                && name.equals(result.name)
                && Objects.equals(message, result.message);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, successful, message);
    }

    @Override
    public String toString() {
        return "CheckerResult{" + "checkerName='" + name + '\'' + ", successful=" + successful + ", message='" + message
            + '\'' + '}';
    }
}
