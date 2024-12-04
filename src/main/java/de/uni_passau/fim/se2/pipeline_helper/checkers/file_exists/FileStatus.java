// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers.file_exists;

enum FileStatus {

    VALID(""),
    MISSING("Missing files:"),
    EMPTY("Empty files:"),
    NON_READABLE("Non-readable files:");

    final String descriptionOnError;

    FileStatus(String descriptionOnError) {
        this.descriptionOnError = descriptionOnError;
    }

    @Override
    public String toString() {
        return descriptionOnError;
    }
}
