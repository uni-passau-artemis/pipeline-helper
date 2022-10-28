// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.model;

public class CheckerException extends Exception {

    public CheckerException(String message) {
        super(message);
    }

    public CheckerException(String message, Throwable cause) {
        super(message, cause);
    }
}
