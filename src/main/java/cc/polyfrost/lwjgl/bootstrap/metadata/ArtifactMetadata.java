package cc.polyfrost.lwjgl.bootstrap.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

/**
 * @author xtrm
 * @since 0.0.1
 */
public class ArtifactMetadata {
    public final @NotNull String groupId;
    public final @NotNull String artifactId;
    public final @NotNull String version;
    public final @Nullable String classifier;
    public final @Nullable String extension;
    public @Nullable String artifactHash;

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
        url += ".sha256";

        this.artifactHash = readUrl(url);
    }

    public @NotNull String getFilename() {
        return artifactId + "-" + version + (classifier != null ? "-" + classifier : "") + "." + (extension != null ? extension : "jar");
    }

    public @NotNull String getMavenPath() {
        return groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + getFilename();
    }

    private static String readUrl(String targetUrl) throws IOException {
        try (Scanner scanner = new Scanner(new URL(targetUrl).openStream(), "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
