// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.model;

public interface Checker {

    /**
     * Executes the actual checker.
     *
     * @return the result of the checking operation. Guaranteed to be a valid feedback sendable to Artemis.
     * @throws CheckerException if the execution failed or an invalid feedback has been constructed.
     */
    CheckerResult check() throws CheckerException;
}
