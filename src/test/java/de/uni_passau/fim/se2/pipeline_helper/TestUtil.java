package de.uni_passau.fim.se2.pipeline_helper;

import java.net.URISyntaxException;
import java.nio.file.Path;

public class TestUtil {

    private TestUtil() {
        throw new UnsupportedOperationException();
    }

    public static Path resource(String resource) throws URISyntaxException {
        return Path.of(TestUtil.class.getClassLoader().getResource(resource).toURI());
    }
}
