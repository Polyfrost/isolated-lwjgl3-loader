package org.polyfrost.lwjgl.isolatedloader.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

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
        HttpURLConnection connection = (HttpURLConnection) new URL(targetUrl).openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(15000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent",
                "isolated-lwjgl3-loader (Java/" +
                        System.getProperty("java.version", "unknown") + "; " +
                        System.getProperty("os.name", "unknown").replace(' ', '_') + " " +
                        System.getProperty("os.version", "unknown").replace(' ', '_') + "; " +
                        System.getProperty("os.arch", "unknown") + ")");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Accept-Encoding", "gzip");

        int code = connection.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
        if (stream == null) {
            stream = new ByteArrayInputStream(new byte[0]);
        }

        String encoding = String.valueOf(connection.getContentEncoding());
        if (encoding.toLowerCase(Locale.ROOT).contains("gzip")) {
            stream = new GZIPInputStream(stream);
        }

        byte[] buf = new byte[8192];
        int r;
        try (InputStream in = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            while ((r = in.read(buf)) != -1) {
                out.write(buf, 0, r);
            }

            String body = out.toString(StandardCharsets.UTF_8.name());
            if (code >= 200 && code < 300) {
                return body;
            }

            throw new IOException("HTTP " + code + " fetching " + targetUrl + (body.isEmpty() ? "" : " — " + body));
        } finally {
            connection.disconnect();
        }
    }
}
