package org.docbook.services.users;

import org.docbook.util.EnvLoader;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class GoogleAuthService {

    private static final String CLIENT_ID = EnvLoader.get("GOOGLE_CLIENT_ID", "");
    private static final String CLIENT_SECRET = EnvLoader.get("GOOGLE_CLIENT_SECRET", "");
    private static final String DEFAULT_REDIRECT_URI = "http://localhost";
    private String redirectUri = DEFAULT_REDIRECT_URI;

    public void setRedirectUri(String uri) {
        this.redirectUri = uri;
    }

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    public String getAuthorizationUrl() {
        return AUTH_URL +
                "?client_id=" + CLIENT_ID +
                "&redirect_uri=" + URLEncoder.encode(this.redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode("email profile", StandardCharsets.UTF_8) +
                "&access_type=offline" +
                "&prompt=consent";
    }

    public GoogleUserInfo exchangeCode(String code) throws IOException, InterruptedException {
        String tokenRequestBody = "code=" + code +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&redirect_uri=" + URLEncoder.encode(this.redirectUri, StandardCharsets.UTF_8) +
                "&grant_type=authorization_code";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(tokenRequestBody))
                .build();

        HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

        if (tokenResponse.statusCode() != 200) {
            throw new IOException("Token exchange failed: " + tokenResponse.body());
        }

        String accessToken = extractJsonValue(tokenResponse.body(), "access_token");

        return fetchUserInfo(client, accessToken);
    }

    private GoogleUserInfo fetchUserInfo(HttpClient client, String accessToken) throws IOException, InterruptedException {
        HttpRequest userInfoRequest = HttpRequest.newBuilder()
                .uri(URI.create(USER_INFO_URL))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> userInfoResponse = client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());

        if (userInfoResponse.statusCode() != 200) {
            throw new IOException("Failed to fetch user info: " + userInfoResponse.body());
        }

        String body = userInfoResponse.body();
        GoogleUserInfo info = new GoogleUserInfo();
        info.setId(extractJsonValue(body, "id"));
        info.setEmail(extractJsonValue(body, "email"));
        info.setName(extractJsonValue(body, "name"));
        info.setPicture(extractJsonValue(body, "picture"));
        info.setVerifiedEmail("true".equals(extractJsonValue(body, "verified_email")));

        return info;
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return "";
        startIndex = json.indexOf(':', startIndex) + 1;
        int valueStart = json.indexOf('"', startIndex) + 1;
        int valueEnd = json.indexOf('"', valueStart);
        return json.substring(valueStart, valueEnd);
    }

    public static class GoogleUserInfo {
        private String id;
        private String email;
        private String name;
        private String picture;
        private boolean verifiedEmail;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPicture() { return picture; }
        public void setPicture(String picture) { this.picture = picture; }
        public boolean isVerifiedEmail() { return verifiedEmail; }
        public void setVerifiedEmail(boolean verifiedEmail) { this.verifiedEmail = verifiedEmail; }
    }
}
