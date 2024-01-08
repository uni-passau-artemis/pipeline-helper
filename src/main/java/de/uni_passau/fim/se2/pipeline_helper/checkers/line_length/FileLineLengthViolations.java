// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers.line_length;

import java.nio.file.Path;
import java.util.List;

public record FileLineLengthViolations(Path file, int count, List<Integer> lines) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String lineOrLines = lines.size() > 1 ? "lines" : "line";
        sb.append(String.format("%s, on %d %s: ", file.toString(), count, lineOrLines));
        if (lines.size() > 1) {
            // append all elements with comma, except the last one, which is appended using '&'
            sb.append(String.join(", ", lines.subList(0, lines.size() - 1).stream().map(Object::toString).toList()));
            sb.append(" & ");
        }
        sb.append(lines.get(lines.size() - 1));
        return sb.toString();
    }
}
