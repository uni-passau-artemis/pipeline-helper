// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.pipeline_helper.checkers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
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

    private record MainMethodInfo(Class<?> cls, int mainMethodCount) {

        @Override
        public String toString() {
            String name = cls.getName();
            if (mainMethodCount == 1) {
                return name;
            }
            else {
                return name + " (x%d)".formatted(mainMethodCount);
            }
        }
    }

    public MainMethodChecker(final Path classpath) {
        this.classpath = classpath;
    }

    @Override
    public CheckerResult check() throws CheckerException {
        try {
            final List<MainMethodInfo> filesWithMainMethods = findMainMethods();
            return getCheckerResult(filesWithMainMethods);
        }
        catch (IOException e) {
            throw new CheckerException("Could not load class files.", e);
        }
    }

    private CheckerResult getCheckerResult(List<MainMethodInfo> filesWithMainMethods) throws CheckerException {
        if (filesWithMainMethods.isEmpty()) {
            return new CheckerResult(CHECKER_NAME, false, "Could not find a main method!");
        }
        else if (filesWithMainMethods.size() == 1 && filesWithMainMethods.get(0).mainMethodCount == 1) {
            final String mainClass = filesWithMainMethods.stream().findFirst().map(MainMethodInfo::toString)
                .orElseThrow();
            System.out.println(mainClass);
            return new CheckerResult(
                CHECKER_NAME,
                true,
                String.format("Found main method in %s", mainClass)
            );
        }
        else {
            final String filesWithMainMethod = filesWithMainMethods.stream()
                .map(MainMethodInfo::toString)
                .collect(Collectors.joining("\n"));
            return new CheckerResult(
                CHECKER_NAME,
                false,
                String.format("Found multiple main methods:%n%s", filesWithMainMethod)
            );
        }
    }

    private List<MainMethodInfo> findMainMethods() throws IOException {
        final URL classPathUrl = classpath.toFile().toURI().toURL();
        try (URLClassLoader cl = new URLClassLoader(new URL[] { classPathUrl })) {
            return FilteredFilesStream.files(classpath, "class")
                .map(this::getClassName)
                .map(className -> loadClass(cl, className))
                .flatMap(Optional::stream)
                .map(this::getMainMethodInfo)
                .filter(mainMethodInfo -> mainMethodInfo.mainMethodCount > 0)
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

    private MainMethodInfo getMainMethodInfo(final Class<?> cls) {
        List<Method> mainMethods = Arrays.stream(cls.getDeclaredMethods())
            .filter(this::isMainMethod).toList();
        return new MainMethodInfo(cls, mainMethods.size());
    }

    private boolean isMainMethod(final Method method) {
        return hasCorrectNameForMainMethod(method) && hasCorrectParametersForMainMethod(method)
            && hasCorrectReturnTypeForMainMethod(method) && hasCorrectModifierForMainMethod(method);
    }

    private boolean hasCorrectNameForMainMethod(final Method method) {
        return "main".equals(method.getName());
    }

    private boolean hasCorrectParametersForMainMethod(final Method method) {
        return method.getParameterCount() == 0
            || method.getParameterCount() == 1 && method.getParameters()[0].getType().equals(String[].class);
    }

    private boolean hasCorrectReturnTypeForMainMethod(final Method method) {
        return method.getReturnType().equals(Void.TYPE);
    }

    private boolean hasCorrectModifierForMainMethod(final Method method) {
        final int modifiers = method.getModifiers();
        return !Modifier.isPrivate(modifiers) && !Modifier.isAbstract(modifiers);
    }
}
