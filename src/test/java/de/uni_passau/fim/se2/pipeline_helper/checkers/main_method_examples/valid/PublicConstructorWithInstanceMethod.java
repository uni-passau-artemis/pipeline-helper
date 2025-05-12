package de.uni_passau.fim.se2.pipeline_helper.checkers.main_method_examples.valid;

import de.uni_passau.fim.se2.pipeline_helper.checkers.main_method_examples.invalid.PrivateConstructorWithInstanceMethod;

public class PublicConstructorWithInstanceMethod extends PrivateConstructorWithInstanceMethod {
    public PublicConstructorWithInstanceMethod() {
        super("");
    }
}
