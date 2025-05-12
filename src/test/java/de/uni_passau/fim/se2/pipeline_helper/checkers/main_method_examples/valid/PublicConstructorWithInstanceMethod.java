// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers.main_method_examples.valid;

import de.uni_passau.fim.se2.pipeline_helper.checkers.main_method_examples.invalid.PrivateConstructorWithInstanceMethod;

public class PublicConstructorWithInstanceMethod extends PrivateConstructorWithInstanceMethod {
    public PublicConstructorWithInstanceMethod() {
        super("");
    }
}
