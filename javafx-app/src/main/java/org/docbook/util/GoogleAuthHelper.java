package org.docbook.util;

import com.sun.net.httpserver.HttpServer;
import org.docbook.services.users.GoogleAuthService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class GoogleAuthHelper {

    public static GoogleAuthService.GoogleUserInfo authenticate() throws Exception {
        GoogleAuthService authService = new GoogleAuthService();

        java.net.ServerSocket socket = new java.net.ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();

        String redirectUri = "http://localhost:" + port + "/callback";
        authService.setRedirectUri(redirectUri);

        System.out.println("Using OAuth callback port: " + port);

        String authUrl = authService.getAuthorizationUrl();
        AtomicReference<String> codeRef = new AtomicReference<>();
        CompletableFuture<Void> future = new CompletableFuture<>();

        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("code=")) {
                int codeIndex = query.indexOf("code=");
                int codeEndIndex = query.indexOf("&", codeIndex);
                String code = codeEndIndex != -1 
                    ? query.substring(codeIndex + 5, codeEndIndex) 
                    : query.substring(codeIndex + 5);
                codeRef.set(code);

                String html = "<html><body><h2>Login successful!</h2><p>You can close this window.</p></body></html>";
                exchange.sendResponseHeaders(200, html.getBytes().length);
                exchange.getResponseBody().write(html.getBytes());
                exchange.close();
                future.complete(null);
            } else {
                String errorHtml = "<html><body><h2>Login failed</h2><p>Error: " + query + "</p></body></html>";
                exchange.sendResponseHeaders(200, errorHtml.getBytes().length);
                exchange.getResponseBody().write(errorHtml.getBytes());
                exchange.close();
                future.complete(null);
                System.err.println("Google auth callback error: " + query);
            }
        });
        server.setExecutor(null);
        server.start();

        java.awt.Desktop.getDesktop().browse(new java.net.URI(authUrl));

        future.get(120, java.util.concurrent.TimeUnit.SECONDS);
        server.stop(0);

        String code = codeRef.get();
        if (code == null) {
            throw new IOException("Authentication timeout or cancelled");
        }

        return authService.exchangeCode(code);
    }
}
