// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.uni_passau.fim.se2.pipeline_helper.helpers.FilteredFilesStream;
import de.uni_passau.fim.se2.pipeline_helper.model.Checker;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerException;
import de.uni_passau.fim.se2.pipeline_helper.model.CheckerResult;

/**
 * Checks that there is exactly one file with a Java main method
 */
public class MainMethodChecker implements Checker {

    private static final String CHECKER_NAME = "MainMethodChecker";

    private final Path classpath;

    public MainMethodChecker(final Path classpath) {
        this.classpath = classpath;
    }

    @Override
    public CheckerResult check() throws CheckerException {
        try {
            final List<Class<?>> filesWithMainMethods = findMainMethods();
            return getCheckerResult(filesWithMainMethods);
        }
        catch (IOException e) {
            throw new CheckerException("Could not load class files.", e);
        }
    }

    private static CheckerResult getCheckerResult(List<Class<?>> filesWithMainMethods) throws CheckerException {
        if (filesWithMainMethods.isEmpty()) {
            return new CheckerResult(CHECKER_NAME, false, "Could not find a main method!");
        }
        else if (filesWithMainMethods.size() == 1) {
            final String mainClass = filesWithMainMethods.stream().findFirst().map(Class::getName).orElseThrow();
            System.out.println(mainClass);
            return new CheckerResult(
                CHECKER_NAME,
                true,
                String.format("Found main method in %s", mainClass)
            );
        }
        else {
            final String filesWithMainMethod = filesWithMainMethods.stream()
                .map(Class::getName)
                .collect(Collectors.joining("\n"));
            return new CheckerResult(
                CHECKER_NAME,
                false,
                String.format("Found multiple main methods:%n%s", filesWithMainMethod)
            );
        }
    }

    private List<Class<?>> findMainMethods() throws IOException {
        final URL classPathUrl = classpath.toFile().toURI().toURL();
        try (URLClassLoader cl = new URLClassLoader(new URL[] { classPathUrl })) {
            return FilteredFilesStream.files(classpath, "class")
                .map(this::getClassName)
                .map(className -> loadClass(cl, className))
                .flatMap(Optional::stream)
                .filter(this::hasMainMethod)
                .toList();
        }
    }

    private String getClassName(final Path classFile) {
        final Path className = classpath.relativize(classFile);
        return className.toString()
            .replace(".class", "")
            .replace("/", ".")
            .replace("\\", ".");
    }

    private Optional<Class<?>> loadClass(final ClassLoader cl, final String className) {
        try {
            final Class<?> cls = cl.loadClass(className);
            return Optional.of(cls);
        }
        catch (ClassNotFoundException | NoClassDefFoundError cnf) {
            return Optional.empty();
        }
    }

    private boolean hasMainMethod(final Class<?> cls) {
        try {
            final Method mainMethod = cls.getDeclaredMethod("main", String[].class);
            return isMainMethod(mainMethod);
        }
        catch (NoSuchMethodException e) {
            return false;
        }
    }

    private boolean isMainMethod(final Method method) {
        return hasCorrectReturnTypeForMainMethod(method);
    }

    private boolean hasCorrectReturnTypeForMainMethod(final Method method) {
        return method.getReturnType().equals(Void.TYPE);
    }
}
