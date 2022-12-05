package cc.polyfrost.lwjgl.bootstrap;

import fr.stardustenterprises.plat4k.EnumFamily;
import fr.stardustenterprises.plat4k.EnumOperatingSystem;
import fr.stardustenterprises.plat4k.Platform;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main class for the LWJGL3 bootstrap library.
 *
 * @author xtrm
 * @since 0.0.1
 */
public enum Lwjgl3Bootstrap {
    /**
     * The singleton instance.
     */
    INSTANCE;

    @NotNull
    private final Platform platform;
    @NotNull
    private Lwjgl3Loader loader;

    Lwjgl3Bootstrap() {
        this.platform = Platform.getCurrentPlatform();
        this.loader = new Lwjgl3Loader(this.getClass().getClassLoader());
    }

    /**
     * Initializes lwjgl3-bootstrap for Minecraft usage.
     *
     * @param minecraftVersion The Minecraft version in context, in padded
     *                         integer form. (ex. 1.12.2 -> 11202)
     * @throws IOException If an I/O error occurs.
     */
    public void initialize(int minecraftVersion) throws IOException {
        // Download the LWJGL3 artifacts
        // Load the LWJGL3 artifacts

        List<Path> jars =
                Lwjgl3Downloader.INSTANCE.ensureDownloaded(minecraftVersion);
        URL[] urls = jars.stream().map(it -> {
            try {
                return it.toUri().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new);
    }

    /**
     * Returns a folder path to cache binaries and other files.
     *
     * @return the cache folder for lwjgl3-bootstrap
     * @throws IOException if the temporary directory is invalid
     */
    private @NotNull Path getBootstrapCache() throws IOException {
        Path cache = getTempDir().resolve("lwjgl3-bootstrap");
        if (Files.notExists(cache)) {
            Files.createDirectories(cache);
        }
        return cache;
    }

    /**
     * Fetches the Operating System's temporary directory Path.
     *
     * @return the {@link Path} to the temporary directory
     * @throws IOException if the temporary directory is invalid
     */
    private @NotNull Path getTempDir() throws IOException {
        EnumOperatingSystem operatingSystem =
                Platform.getCurrentPlatform().getOperatingSystem();

        String defaultTempDir;
        if (EnumFamily.WINDOWS.contains(operatingSystem)) {
            defaultTempDir = System.getenv("TEMP");
            if (defaultTempDir == null) {
                defaultTempDir = System.getenv("TMP");
                if (defaultTempDir == null) {
                    defaultTempDir = "C:\\Windows\\Temp";
                    if (!new File(defaultTempDir).exists()) {
                        defaultTempDir = "C:\\Temp";
                    }
                }
            }
        } else if (EnumFamily.UNIX.contains(operatingSystem)) {
            defaultTempDir = System.getenv("TMPDIR");
            if (defaultTempDir == null) {
                defaultTempDir = "/tmp";
            }
        } else {
            defaultTempDir = "";
        }

        try {
            Path path = Paths.get(System.getProperty("java.io.tmpdir", defaultTempDir));
            if (Files.exists(path)) {
                return path;
            }
            throw new IOException("Couldn't find temporary directory: " + path);
        } catch (InvalidPathException exception) {
            throw new IOException("Invalid temporary directory path.", exception);
        }
    }
}
