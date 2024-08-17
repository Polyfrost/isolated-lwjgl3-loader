package dev.deftu.lwjgl.bootstrap.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author xtrm
 * @since 0.0.1
 */
public final class ArtifactMetadata {

    private final @NotNull String groupId;
    private final @NotNull String artifactId;
    private final @NotNull String version;
    private final @Nullable String classifier;
    private final @Nullable String extension;

    /**
     * The sha-1 hash of the artifact, if resolved.
     *
     * @see #resolveHash(URL)
     * @return the sha-1 hash of the artifact, if resolved.
     */
    private @Nullable String artifactHash;

    public ArtifactMetadata(@NotNull String groupId, @NotNull String artifactId, @NotNull String version) {
        this(groupId, artifactId, version, null, null);
    }

    public ArtifactMetadata(@NotNull String groupId, @NotNull String artifactId, @NotNull String version, @Nullable String classifier) {
        this(groupId, artifactId, version, classifier, null);
    }

    public ArtifactMetadata(@NotNull String groupId, @NotNull String artifactId, @NotNull String version, @Nullable String classifier, @Nullable String extension) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.extension = extension == null ? "jar" : extension;
    }

    public void resolveHash(@NotNull URL mavenUrl) throws IOException {
        if (this.artifactHash != null)
            return;

        String url = mavenUrl.toString();
        if (!url.endsWith("/"))
            url = url + "/";
        url += getMavenPath();
        url += ".sha1";

        this.artifactHash = readUrl(url);
    }

    @Nullable
    public String getArtifactHash() {
        return artifactHash;
    }

    public @NotNull String getFileName() {
        return artifactId + "-" + version + (classifier != null ? "-" + classifier : "") + "." + (extension != null ? extension : "jar");
    }

    public @NotNull String getMavenPath() {
        return groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + getFileName();
    }

    public @NotNull String getArtifactDeclaration() {
        return groupId + ":" + artifactId + ":" + version + (classifier != null ? ":" + classifier : "") + (extension != null && !extension.equals("jar") ? ":" + extension : "");
    }

    @Override
    public String toString() {
        return "ArtifactMetadata{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", classifier='" + classifier + '\'' +
                ", extension='" + extension + '\'' +
                ", artifactHash='" + artifactHash + '\'' +
                '}';
    }

    private static String readUrl(String targetUrl) throws IOException {
        try (Scanner scanner = new Scanner(new URL(targetUrl).openStream(), StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

}
