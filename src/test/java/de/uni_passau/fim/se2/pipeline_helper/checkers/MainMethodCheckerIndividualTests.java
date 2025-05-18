// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

public class MainMethodCheckerIndividualTests {

    private static final Path VALID_DIR = Paths
        .get("src/test/java/de/uni_passau/fim/se2/pipeline_helper/checkers/main_method_examples/valid");
    private static final Path INVALID_DIR = Paths
        .get("src/test/java/de/uni_passau/fim/se2/pipeline_helper/checkers/main_method_examples/invalid");

    private static Stream<Path> getStream(Path dir) throws IOException {
        return Files.walk(dir)
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java") && !p.getFileName().toString().equals("package-info.java"));
    }

    private static Stream<Path> getValidSources() throws IOException {
        return getStream(VALID_DIR);
    }

    private static Stream<Path> getInvalidSources() throws IOException {
        return getStream(INVALID_DIR);
    }

    private static void compile(Path source, Path outputDirectory) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String classPath = Arrays.stream(System.getProperty("java.class.path").split(":"))
            .filter(path -> !path.contains("target/test-classes"))
            .collect(Collectors.joining(":"));

        try (
            StandardJavaFileManager fm = compiler
                .getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8)
        ) {
            compiler.getTask(
                null, fm, null,
                List.of(
                    "-classpath", classPath,
                    "-sourcepath", "src/test/java",
                    "-d", outputDirectory.toString(), "-implicit:class"
                ),
                null,
                fm.getJavaFileObjectsFromFiles(List.of(source.toFile()))
            ).call();
        }
    }

    private static CheckerResult copyCompileAndCheck(Path sourceFile, Path tempDir)
        throws IOException, CheckerException {
        compile(sourceFile, tempDir);
        MainMethodChecker checker = new MainMethodChecker(tempDir);
        return checker.check();
    }

    @ParameterizedTest(name = "[INVALID] {0}")
    @MethodSource("getInvalidSources")
    void shouldRejectInvalidMainMethod(Path sourceFile, @TempDir Path tempDir) throws IOException, CheckerException {
        CheckerResult result = copyCompileAndCheck(sourceFile, tempDir);
        assertFalse(result.isSuccessful());
    }

    @ParameterizedTest(name = "[VALID] {0}")
    @MethodSource("getValidSources")
    void shouldAcceptValidMainMethod(Path sourceFile, @TempDir Path tempDir) throws IOException, CheckerException {
        CheckerResult result = copyCompileAndCheck(sourceFile, tempDir);
        assertTrue(result.isSuccessful());
    }
}
