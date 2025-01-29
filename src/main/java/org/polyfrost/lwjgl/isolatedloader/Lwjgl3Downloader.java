package org.polyfrost.lwjgl.isolatedloader;

import org.polyfrost.polyio.api.Downloader;
import org.polyfrost.polyio.api.Store;
import org.polyfrost.polyio.store.FastHashSchema;
import org.polyfrost.polyio.util.HashingHelper;
import org.polyfrost.lwjgl.isolatedloader.metadata.ArtifactMetadata;
import org.polyfrost.lwjgl.isolatedloader.metadata.PlatformMetadata;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Lwjgl3Downloader {

    private static final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2/";

    private static final String LWJGL3_ARTIFACT_ID = "lwjgl-%s";
    private static final String LWJGL_SYSTEM_MODULE = "lwjgl";

    private static final Downloader DOWNLOADER;
    private static final Store LIBRARY_STORE;
    private static final URL MAVEN_REPOSITORY;

    private static boolean isSystemModuleDownloaded = false;
    private static final Set<String> downloadedModules = new HashSet<>();

    private Lwjgl3Downloader() {
    }

    /**
     * Downloads the specified LWJGL modules.
     *
     * @param lwjglModules the LWJGL modules to download
     * @return the paths to the downloaded modules
     * @throws IOException if an I/O error occurs
     */
    public static Set<Path> downloadJars(String[] lwjglModules) throws IOException {
        if (downloadedModules.containsAll(Arrays.asList(lwjglModules))) {
            return new HashSet<>();
        }

        String[] newModules = Arrays.stream(lwjglModules)
                .filter(module -> !downloadedModules.contains(module))
                .toArray(String[]::new);
        PlatformMetadata platformMetadata = PlatformMetadata.from();
        Set<ArtifactMetadata> remoteMetadata = getArtifactsFor(platformMetadata, newModules);
        Set<Downloader.Download<URL>> downloads = new HashSet<>();

        for (ArtifactMetadata artifactMetadata : remoteMetadata) {
            artifactMetadata.resolveHash(MAVEN_REPOSITORY);

            URL url = new URL(MAVEN_REPOSITORY, artifactMetadata.getMavenPath());
            downloads.add(DOWNLOADER.download(
                    url,
                    LIBRARY_STORE.getObject(
                            artifactMetadata.getArtifactDeclaration()
                    ),
                    Downloader.HashProvider.of(
                            artifactMetadata.getArtifactHash(),
                            HashingHelper.SHA1
                    ),
                    Downloader.DownloadCallback.NOOP
            ));
        }

        Set<Path> paths = downloads.stream()
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

        downloadedModules.addAll(Arrays.asList(lwjglModules));
        return paths;
    }

    /**
     * Downloads the native libraries for the specified LWJGL modules.
     *
     * @param lwjglModules the LWJGL modules to download the natives
     * @return the paths to the downloaded natives
     * @throws IOException
     */
    public static Set<Path> downloadNatives(String[] lwjglModules) throws IOException {
        PlatformMetadata platformMetadata = PlatformMetadata.from();
        Set<ArtifactMetadata> remoteMetadata = getNativeArtifactsFor(platformMetadata, lwjglModules);
        Set<Downloader.Download<URL>> downloads = new HashSet<>();

        for (ArtifactMetadata artifactMetadata : remoteMetadata) {
            artifactMetadata.resolveHash(MAVEN_REPOSITORY);

            URL url = new URL(MAVEN_REPOSITORY, artifactMetadata.getMavenPath());
            downloads.add(DOWNLOADER.download(
                    url,
                    LIBRARY_STORE.getObject(
                            artifactMetadata.getArtifactDeclaration()
                    ),
                    Downloader.HashProvider.of(
                            artifactMetadata.getArtifactHash(),
                            HashingHelper.SHA1
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

        if (!isSystemModuleDownloaded && !ignoreSystemModule()) {
            artifacts.add(lwjglArtifact(LWJGL_SYSTEM_MODULE, platformMetadata));
            isSystemModuleDownloaded = true;
        }

        for (String lwjglModule : lwjglModules) {
            artifacts.add(lwjglArtifact(String.format(LWJGL3_ARTIFACT_ID, lwjglModule), platformMetadata));
        }

        return artifacts;
    }

    public static Set<ArtifactMetadata> getNativeArtifactsFor(PlatformMetadata platformMetadata, String[] lwjglModules) {
        Set<ArtifactMetadata> artifacts = new HashSet<>();

        for (String lwjglModule : lwjglModules) {
            artifacts.add(lwjglNativeArtifact(String.format(LWJGL3_ARTIFACT_ID, lwjglModule), platformMetadata));
        }

        return artifacts;
    }

    private static ArtifactMetadata lwjglArtifact(String moduleName, PlatformMetadata platformMeta) {
        return new ArtifactMetadata(
                "org.lwjgl",
                moduleName,
                platformMeta.getLwjglVersion()
        );
    }

    private static ArtifactMetadata lwjglNativeArtifact(String moduleName, PlatformMetadata platformMetadata) {
        return new ArtifactMetadata(
                "org.lwjgl",
                moduleName,
                platformMetadata.getLwjglVersion(),
                platformMetadata.getLwjglNativeClassifier()
        );
    }

    private static boolean ignoreSystemModule() {
        return Boolean.getBoolean("isolatedlwjgl3loader.ignoreSystemModule");
    }

    static {
        try {
            MAVEN_REPOSITORY = new URL(MAVEN_CENTRAL_URL);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to initialize LWJGL3 downloader", t);
        }

        Store downloadStore = Lwjgl3Manager.getStore().getSubStore(".cache", new FastHashSchema(HashingHelper.MD5));
        DOWNLOADER = Downloader.create(downloadStore);
        LIBRARY_STORE = Lwjgl3Manager.getStore().getSubStore("libraries", Store.ObjectSchema.MAVEN);
    }

}
