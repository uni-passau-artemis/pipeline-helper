package de.uni_passau.fim.se2.pipeline_helper.model;

public class CheckerException extends Exception {
    public CheckerException(String message) {
        super(message);
    }

    public CheckerException(String message, Throwable cause) {
        super(message, cause);
    }
}
