package dev.deftu.lwjgl.bootstrap;

import cc.polyfrost.polyio.api.Downloader;
import cc.polyfrost.polyio.api.Store;
import cc.polyfrost.polyio.download.PolyDownloader;
import cc.polyfrost.polyio.store.FastHashSchema;
import cc.polyfrost.polyio.util.PolyHashing;
import dev.deftu.lwjgl.bootstrap.metadata.ArtifactMetadata;
import dev.deftu.lwjgl.bootstrap.metadata.PlatformMetadata;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Lwjgl3Downloader {

    private static final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2/";

    private static final String LWJGL3_ARTIFACT_ID = "lwjgl-%s";
    private static final String LWJGL_SYSTEM_MODULE = "lwjgl";

    private static final Downloader downloader;
    private static final Store libraryStore;
    private static final URL mavenRepository;

    private Lwjgl3Downloader() {
    }

    public static Set<Path> download(int paddedMinecraftVersion, String[] lwjglModules) throws IOException {
        PlatformMetadata platformMetadata = PlatformMetadata.from(paddedMinecraftVersion);
        Set<ArtifactMetadata> remoteMetadata = getArtifactsFor(platformMetadata, lwjglModules);
        Set<Downloader.Download<URL>> downloads = new HashSet<>();

        for (ArtifactMetadata artifactMetadata : remoteMetadata) {
            artifactMetadata.resolveHash(mavenRepository);

            URL url = new URL(mavenRepository, artifactMetadata.getMavenPath());
            downloads.add(downloader.download(
                    url,
                    libraryStore.getObject(
                            artifactMetadata.getArtifactDeclaration()
                    ),
                    Downloader.HashProvider.of(
                            artifactMetadata.getArtifactHash(),
                            "SHA-1"
                    ),
                    Downloader.DownloadCallback.NOOP
            ));
        }

        return downloads.stream()
                .map(download -> {
                    try {
                        return download.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(String.format(
                                "Failed to download %s",
                                download.getSource()
                        ), e);
                    }
                })
                .collect(Collectors.toSet());
    }

    public static Set<ArtifactMetadata> getArtifactsFor(PlatformMetadata platformMetadata, String[] lwjglModules) {
        Set<ArtifactMetadata> artifacts = new HashSet<>();
        if (platformMetadata.isRequiresSystemPlatform()) {
            artifacts.addAll(lwjglArtifacts(LWJGL_SYSTEM_MODULE, platformMetadata));
        }

        for (String lwjglModule : lwjglModules) {
            artifacts.addAll(lwjglArtifacts(String.format(LWJGL3_ARTIFACT_ID, lwjglModule), platformMetadata));
        }

        return artifacts;
    }

    private static Collection<ArtifactMetadata> lwjglArtifacts(String moduleName, PlatformMetadata platformMeta) {
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

    static {
        try {
            mavenRepository = new URL(MAVEN_CENTRAL_URL);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to initialize LWJGL3 downloader", t);
        }

        Store downloadStore = Lwjgl3Bootstrap.getStore().getSubStore(".cache", new FastHashSchema(PolyHashing.MD5));
        downloader = new PolyDownloader(downloadStore);
        libraryStore = Lwjgl3Bootstrap.getStore().getSubStore("libraries", Store.ObjectSchema.MAVEN);
    }

}
