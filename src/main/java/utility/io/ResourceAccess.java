package utility.io;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Centralized access to runtime data bundled on the application classpath.
 */
public final class ResourceAccess {

    private ResourceAccess() {
    }

    /**
     * Open a UTF-8 reader for a classpath resource.
     *
     * @param path absolute classpath path, e.g. {@code /Resources/foo.json}
     * @return UTF-8 reader for the resource
     */
    public static Reader reader(String path) {
        return new InputStreamReader(stream(path), StandardCharsets.UTF_8);
    }

    /**
     * Open an input stream for a classpath resource.
     *
     * @param path absolute classpath path, e.g. {@code /Resources/foo.json}
     * @return resource stream
     */
    public static InputStream stream(String path) {
        InputStream stream = ResourceAccess.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalArgumentException("Missing classpath resource: " + path);
        }
        return stream;
    }

    /**
     * Check whether a classpath resource exists.
     *
     * @param path absolute classpath path, e.g. {@code /Resources/foo.json}
     * @return true when the resource is available on the classpath
     */
    public static boolean exists(String path) {
        return ResourceAccess.class.getResource(path) != null;
    }
}
