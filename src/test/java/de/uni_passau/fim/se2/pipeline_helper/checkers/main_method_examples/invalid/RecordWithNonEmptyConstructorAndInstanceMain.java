// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers.main_method_examples.invalid;

public record RecordWithNonEmptyConstructorAndInstanceMain(int a, int b, int c) {
    public void main() {
    }
}
