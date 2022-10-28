package de.uni_passau.fim.se2.pipeline_helper.checkers;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.pipeline_helper.helpers.FilteredFilesStream;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

class MainMethodCheckerTest {

    @Test
    void hasMainMethodPositive() {
        final MainMethodChecker checker = new MainMethodChecker(Stream.<Path>builder().build());

        assertThat(checker.hasMainMethod("""
            public static void main(String... args) {""")).isTrue();

        assertThat(checker.hasMainMethod("""
            public static void main(java.lang.String... args) {""")).isTrue();

        assertThat(checker.hasMainMethod("""
            public static void main(java.lang.String... a897345nfDHS) {""")).isTrue();

        assertThat(checker.hasMainMethod("""
            public static void main(String[] args)
            {""")).isTrue();

        assertThat(checker.hasMainMethod("""
            public   static \tvoid
            main(String...          args){""")).isTrue();

        assertThat(checker.hasMainMethod("""
            package dsad;
            public static void main(String[] args) {""")).isTrue();

        assertThat(checker.hasMainMethod("""
            public static void main(String[] args) throws IOException {""")).isTrue();

        assertThat(checker.hasMainMethod("""
            public static void main(String[] args) throws IOException,RandomException{""")).isTrue();
    }

    @Test
    void hasMainMethodNegative() {
        final MainMethodChecker checker = new MainMethodChecker(Stream.<Path>builder().build());

        assertThat(checker.hasMainMethod("""
            static void main(String[] args) {""")).isFalse();

        assertThat(checker.hasMainMethod("""
            public static void main(String[] args, int a) {""")).isFalse();

        assertThat(checker.hasMainMethod("""
            public static void main(Integer s) {""")).isFalse();

        assertThat(checker.hasMainMethod("""
            public static void main(java.lang.String... 897345nfDHS) {""")).isFalse();

        // not in comments
        assertThat(checker.hasMainMethod("""
            // public static void main(String[] args) {""")).isFalse();
        assertThat(checker.hasMainMethod("""
            // 6\t public static void main(String[] args) {""")).isFalse();
        assertThat(checker.hasMainMethod("""
            * public static void main(String[] args) {""")).isFalse();
        assertThat(checker.hasMainMethod("""
            *   8rb90   public static void main(String[] args) {""")).isFalse();
    }

    @Test
    void checkerSingleMainMethod() throws IOException, CheckerException {
        final MainMethodChecker checker = new MainMethodChecker(
            FilteredFilesStream.files(Path.of("src/main/java/"), "java")
        );
        final CheckerResult result = checker.check();

        final CheckerResult expectedResult = new CheckerResult(
            "MainMethodChecker", true,
            "Found main method in main.java.de.uni_passau.fim.se2.pipeline_helper.Main"
        );
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void checkerMultipleMainMethods() throws IOException, CheckerException {
        final MainMethodChecker checker = new MainMethodChecker(FilteredFilesStream.files(Path.of("src/"), "java"));
        final CheckerResult result = checker.check();

        final CheckerResult expectedResult = new CheckerResult("MainMethodChecker", false, """
            Found multiple files with main methods:
            test.resources.line_length_checker_demo_files.valid.ValidFile
            main.java.de.uni_passau.fim.se2.pipeline_helper.Main
            test.java.de.uni_passau.fim.se2.pipeline_helper.checkers.MainMethodCheckerTest
            test.resources.line_length_checker_demo_files.invalid.InvalidFile""");
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void checkerNoMainMethods() throws IOException, CheckerException {
        final MainMethodChecker checker = new MainMethodChecker(
            FilteredFilesStream
                .files(Path.of("src/main/java/de/uni_passau/fim/se2/pipeline_helper/checkers"), "java")
        );
        final CheckerResult result = checker.check();

        final CheckerResult expectedResult = new CheckerResult(
            "MainMethodChecker", false,
            "Could not find a main method!"
        );
        assertThat(result).isEqualTo(expectedResult);
    }

    public static void main(String[] args) {
        System.exit(0);
    }
}
