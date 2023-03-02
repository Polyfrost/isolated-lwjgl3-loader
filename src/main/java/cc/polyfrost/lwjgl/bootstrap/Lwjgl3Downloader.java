package cc.polyfrost.lwjgl.bootstrap;

import cc.polyfrost.lwjgl.bootstrap.metadata.ArtifactMetadata;
import cc.polyfrost.lwjgl.bootstrap.metadata.PlatformMetadata;
import cc.polyfrost.polyio.api.Downloader;
import cc.polyfrost.polyio.api.Store;
import cc.polyfrost.polyio.download.PolyDownloader;
import cc.polyfrost.polyio.store.FastHashSchema;
import cc.polyfrost.polyio.util.PolyHashing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author xtrm
 * @since 0.0.1
 */
@VisibleForTesting
enum Lwjgl3Downloader {
    /**
     * The singleton instance.
     */
    INSTANCE;

    private static final String MAVEN_CENTRAL_URL =
            "https://repo1.maven.org/maven2/";

    private static final String LWJGL3_GROUP_ID =
            "org.lwjgl";
    private static final String LWJGL3_ARTIFACT_ID =
            "lwjgl-%s";
    private static final String LWJGL_SYSTEM_MODULE = "lwjgl";
    private static final String[] LWJGL_MODULES =
            new String[]{"nanovg", "tinyfd", "stb"};

    /**
     * The downloader instance.
     */
    private final Downloader downloader;
    private final Store librariesStore;
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
        Store downloadStore = Lwjgl3Bootstrap.INSTANCE.getStore().getSubStore(
                ".cache",
                new FastHashSchema(PolyHashing.MD5)
        );
        this.downloader = new PolyDownloader(downloadStore);
        this.librariesStore = Lwjgl3Bootstrap.INSTANCE.getStore()
                .getSubStore("libraries", Store.ObjectSchema.MAVEN);
    }

    /**
     * Ensures that the LWJGL3 artifacts are downloaded and returns the path to
     * the downloaded jar(s).
     * <p>
     * This method might perform additional hash verification to prevent
     * unnecessary downloads.
     *
     * @param gameVer The Minecraft version in context, in padded
     *                integer form. (ex. 1.12.2 -> 11202)
     * @return The path to the downloaded jar(s).
     * @throws IOException If an I/O error occurs.
     */
    public @NotNull List<Path> ensureDownloaded(
            int gameVer
    ) throws IOException {
        PlatformMetadata platformMetadata = PlatformMetadata.from(gameVer);

        List<ArtifactMetadata> remoteMetadata = Lwjgl3Downloader.INSTANCE
                .requiredFor(platformMetadata);
        List<Downloader.Download<URL>> downloads = new ArrayList<>();
        for (ArtifactMetadata artifactMetadata : remoteMetadata) {
            artifactMetadata.resolveHash(mavenRepository);

            URL url = new URL(mavenRepository, artifactMetadata.getMavenPath());
            downloads.add(
                    downloader.download(
                            url,
                            librariesStore.getObject(
                                    artifactMetadata.getArtifactDeclaration()
                            ),
                            Downloader.HashProvider.of(
                                    artifactMetadata.getArtifactHash(),
                                    "SHA-1"
                            ),
                            Downloader.DownloadCallback.NOOP
                    )
            );
        }

        return downloads.stream()
                .map(it -> {
                    try {
                        return it.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Builds a list containing the required LWJGL3 artifacts.
     *
     * @param platformMeta The platform metadata.
     * @return The list of required artifacts for this context.
     */
    public List<ArtifactMetadata> requiredFor(
            PlatformMetadata platformMeta
    ) {
        List<ArtifactMetadata> artifacts = new ArrayList<>();

        if (platformMeta.isRequiresSystemPlatform()) {
            artifacts.addAll(lwjglArtifacts(LWJGL_SYSTEM_MODULE, platformMeta));
        }
        for (String module : LWJGL_MODULES) {
            artifacts.addAll(lwjglArtifacts(String.format(LWJGL3_ARTIFACT_ID, module), platformMeta));
        }

        return artifacts;
    }

    private Collection<ArtifactMetadata> lwjglArtifacts(
            String moduleName,
            PlatformMetadata platformMeta
    ) {
        return Arrays.asList(
                new ArtifactMetadata(
                        "org.lwjgl",
                        moduleName,
                        platformMeta.getLwjglVersion()
                ),
                new ArtifactMetadata(
                        "org.lwjgl",
                        moduleName,
                        platformMeta.getLwjglVersion(),
                        platformMeta.getLwjglNativeClassifier()
                )
        );
    }
}
