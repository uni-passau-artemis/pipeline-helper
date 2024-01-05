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
        sb.append(String.format("%s, on %d lines: ", file.toString(), count));
        lines.forEach(line -> sb.append(String.format("%d, ", line)));
        
        // remove last whitespace and comma
        sb.replace(sb.length() - 2, sb.length(), "");
        
        // replace last comma with '&' for a nicer look
        sb.replace(sb.length() - 3, sb.length() - 1, " & ");
        sb.append(String.format("%n"));
        return sb.toString();
    }
}
