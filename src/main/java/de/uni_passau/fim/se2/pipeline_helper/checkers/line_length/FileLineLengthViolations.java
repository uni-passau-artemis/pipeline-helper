// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers.line_length;

import java.nio.file.Path;
import java.util.SortedMap;

public record FileLineLengthViolations(Path file, SortedMap<Integer, Integer> violationsWithLength) {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String lineOrLines = violationsWithLength.size() > 1 ? "lines" : "line";
        sb.append(String.format("%s, on %d %s:%n", file.toString(), violationsWithLength.size(), lineOrLines));
        violationsWithLength.forEach((index, length) -> {
            sb.append(String.format("    -> line %d, length %d%n", index, length));
        });
        return sb.toString();
    }
}
