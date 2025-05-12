package de.uni_passau.fim.se2.pipeline_helper.checkers;

import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainMethodCheckerIndividualTests {
    private static final Path VALID_DIR = Paths.get("src/test/java/de/uni_passau/fim/se2/pipeline_helper/checkers/main_method_examples/valid");
    private static final Path INVALID_DIR = Paths.get("src/test/java/de/uni_passau/fim/se2/pipeline_helper/checkers/main_method_examples/invalid");

    private static Stream<Path> getValidSources() throws IOException {
        return Files.walk(VALID_DIR)
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"));
    }

    private static Stream<Path> getInvalidSources() throws IOException {
        return Files.walk(INVALID_DIR)
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"));
    }

    private static void compile(Path source, Path outputDirectory) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fm = compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8)) {
            fm.setLocation(StandardLocation.CLASS_OUTPUT, List.of(outputDirectory.toFile()));
            compiler.getTask( null, fm, null,
                    List.of("-classpath", System.getProperty("java.class.path")),
                    null,
                    fm.getJavaFileObjectsFromFiles(List.of(source.toFile()))
            ).call();
        }
    }

    private static CheckerResult copyCompileAndCheck(Path sourceFile, Path tempDir) throws IOException, CheckerException {
        Path targetFile = tempDir.resolve(sourceFile.getFileName());
        targetFile.toFile().delete();
        Path tempFile = Files.copy(sourceFile, targetFile);
        compile(tempFile, tempDir);
        MainMethodChecker checker = new MainMethodChecker(tempDir);
        CheckerResult result = checker.check();
        Files.delete(tempFile);
        return result;
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

    // TODO: Check for project java version so the right compiler version is always available.
}
