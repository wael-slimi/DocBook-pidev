package org.docbook.util;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;

public class AvatarService {

    private static String AVATAR_DIR;

    private static String getAvatarDir() {
        if (AVATAR_DIR == null) {
            try {
                Path baseDir = Paths.get(System.getProperty("user.dir")).resolve("assets").resolve("avatars");
                Files.createDirectories(baseDir);
                try {
                    Files.setPosixFilePermissions(baseDir, java.util.Set.of(
                        java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                        java.nio.file.attribute.PosixFilePermission.OWNER_WRITE,
                        java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE,
                        java.nio.file.attribute.PosixFilePermission.GROUP_READ,
                        java.nio.file.attribute.PosixFilePermission.GROUP_WRITE,
                        java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE,
                        java.nio.file.attribute.PosixFilePermission.OTHERS_READ,
                        java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE,
                        java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE
                    ));
                } catch (UnsupportedOperationException e) {
                }
                AVATAR_DIR = baseDir.toAbsolutePath().toString();
                System.out.println("Avatar directory initialized: " + AVATAR_DIR);
            } catch (Exception e) {
                System.err.println("Failed to create avatar directory: " + e.getMessage());
                AVATAR_DIR = System.getProperty("java.io.tmpdir");
                System.out.println("Falling back to temp dir: " + AVATAR_DIR);
            }
        }
        return AVATAR_DIR;
    }

    public static String getAvatarPath(int userId) {
        String dir = getAvatarDir();
        Path userAvatar = Paths.get(dir, userId + ".png");
        if (Files.exists(userAvatar)) {
            return userAvatar.toAbsolutePath().toString();
        }
        Path defaultPath = Paths.get(dir, "default-avatar.png");
        if (Files.exists(defaultPath)) {
            return defaultPath.toAbsolutePath().toString();
        }
        return null;
    }

    public static void saveAvatarFromFile(int userId, File sourceFile) throws IOException {
        String dir = getAvatarDir();
        Path destPath = Paths.get(dir, userId + ".png");
        try (java.io.InputStream in = new java.io.FileInputStream(sourceFile)) {
            Files.copy(in, destPath, StandardCopyOption.REPLACE_EXISTING);
        }
        setPermissions(destPath);
        System.out.println("Avatar uploaded: " + destPath.toAbsolutePath());
    }

    public static void generateAndSaveAvatar(int userId, String description) throws IOException, InterruptedException {
        String dir = getAvatarDir();
        String width = EnvLoader.get("AI_AVATAR_WIDTH", "256");
        String height = EnvLoader.get("AI_AVATAR_HEIGHT", "256");
        String prompt = URLEncoder.encode(description, "UTF-8");
        String url = EnvLoader.get("AI_AVATAR_API_URL", "https://image.pollinations.ai/prompt")
                + "/" + prompt + "?width=" + width + "&height=" + height + "&nologo=true&seed=" + System.currentTimeMillis();

        System.out.println("Fetching AI avatar from: " + url);

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "image/png")
                .timeout(java.time.Duration.ofMinutes(2))
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        System.out.println("Response status: " + response.statusCode());
        System.out.println("Content-Type: " + response.headers().firstValue("Content-Type").orElse("unknown"));

        if (response.statusCode() == 200) {
            Path destPath = Paths.get(dir, userId + ".png");
            byte[] data = response.body().readAllBytes();
            System.out.println("Received " + data.length + " bytes");
            Files.write(destPath, data);
            setPermissions(destPath);
            System.out.println("AI avatar saved: " + destPath.toAbsolutePath());
        } else {
            throw new IOException("Failed to generate avatar. HTTP status: " + response.statusCode());
        }
    }

    public static void deleteAvatar(int userId) {
        String dir = getAvatarDir();
        Path userAvatar = Paths.get(dir, userId + ".png");
        try {
            Files.deleteIfExists(userAvatar);
            System.out.println("Avatar deleted for user " + userId);
        } catch (IOException e) {
            System.err.println("Failed to delete avatar: " + e.getMessage());
        }
    }

    private static void setPermissions(Path path) {
        try {
            Files.setPosixFilePermissions(path, java.util.Set.of(
                java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                java.nio.file.attribute.PosixFilePermission.OWNER_WRITE,
                java.nio.file.attribute.PosixFilePermission.GROUP_READ,
                java.nio.file.attribute.PosixFilePermission.GROUP_WRITE,
                java.nio.file.attribute.PosixFilePermission.OTHERS_READ,
                java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE
            ));
        } catch (UnsupportedOperationException | IOException e) {
        }
    }
}
