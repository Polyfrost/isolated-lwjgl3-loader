package cc.polyfrost.lwjgl.bootstrap;

import cc.polyfrost.lwjgl.bootstrap.metadata.ArtifactMetadata;
import cc.polyfrost.lwjgl.bootstrap.metadata.PlatformMetadata;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author xtrm
 * @since 0.0.1
 */
enum Lwjgl3Downloader {
    /**
     * The singleton instance.
     */
    INSTANCE;

    private static final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2/";
    private static final String[] LWJGL_MODULES = {"nanovg", "tinyfd", "stb"};

    /**
     * The maven repository URL from which to fetch LWJGL3.
     */
    private final URL mavenRepository;

    Lwjgl3Downloader() {
        try {
            this.mavenRepository = new URL(MAVEN_CENTRAL_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Ensures that the LWJGL3 artifacts are downloaded and returns the path to
     * the downloaded jar(s).
     * <p>
     * This method might perform additional hash verification to prevent
     * unnecessary downloads.
     *
     * @param minecraftVersion The Minecraft version in context, in padded
     *                         integer form. (ex. 1.12.2 -> 11202)
     * @return The path to the downloaded jar(s).
     * @throws IOException If an I/O error occurs.
     */
    public @NotNull List<Path> ensureDownloaded(int minecraftVersion) throws IOException {
        PlatformMetadata platformMetadata = PlatformMetadata.from(minecraftVersion);

        List<ArtifactMetadata> remoteMetadata = Lwjgl3Downloader.INSTANCE.fetchArtifactsInfo(platformMetadata);
        return null;
    }

    /**
     * Fetches the LWJGL3 artifacts from the remote repository.
     *
     * @param platformMeta The platform metadata.
     * @return The list of required artifacts for this context.
     * @throws IOException If an I/O error occurs.
     */
    public List<ArtifactMetadata> fetchArtifactsInfo(PlatformMetadata platformMeta) throws IOException {
        List<ArtifactMetadata> artifacts = new ArrayList<>();

        if (platformMeta.requiresSystemPlatform) {
            artifacts.addAll(lwjglArtifacts("lwjgl", platformMeta));
        }
        for (String module : LWJGL_MODULES) {
            artifacts.addAll(lwjglArtifacts(module, platformMeta));
        }

        for (ArtifactMetadata it : artifacts) {
            it.resolveHash(mavenRepository);
        }

        return artifacts;
    }

    private Collection<ArtifactMetadata> lwjglArtifacts(String moduleName, PlatformMetadata platformMeta) {
        return Arrays.asList(
                new ArtifactMetadata(
                        "org.lwjgl",
                        "lwjgl-" + moduleName,
                        platformMeta.lwjglVersion
                ),
                new ArtifactMetadata(
                        "org.lwjgl",
                        "lwjgl-" + moduleName,
                        platformMeta.lwjglVersion,
                        platformMeta.lwjglNativeClassifier
                )
        );
    }

    private ArtifactMetadata lwjglNativesArtifact(String moduleName, PlatformMetadata platformMeta) {
    }
}
