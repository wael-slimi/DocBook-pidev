package org.docbook.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.docbook.entities.records.Document;
import org.docbook.entities.records.DossierMedical;
import org.docbook.services.medical.DocumentService;
import org.docbook.services.medical.DossierMedicalService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalWebServerService {

    private static LocalWebServerService instance;

    public static synchronized LocalWebServerService getInstance() {
        if (instance == null) {
            instance = new LocalWebServerService();
        }
        return instance;
    }

    private HttpServer server;
    private int port = 8080;
    private String localIp = "127.0.0.1";
    private boolean started = false;

    private LocalWebServerService() {
    }

    public synchronized void startServer() {
        if (started) {
            return;
        }

        localIp = getLocalIpAddress();

        boolean bound = false;
        for (int p = 8080; p <= 8099; p++) {
            try {
                server = HttpServer.create(new InetSocketAddress("0.0.0.0", p), 0);
                this.port = p;
                bound = true;
                break;
            } catch (IOException e) {
                System.err.println("[LocalWebServer] Port " + p + " occupé, essai suivant...");
            }
        }

        if (bound) {
            server.createContext("/document", new DocumentHandler());
            server.createContext("/patient", new PatientHandler());
            server.setExecutor(null);
            server.start();
            started = true;
            System.out.println("[LocalWebServer] SUCCÈS ! Serveur démarré sur : http://" + localIp + ":" + port);
        } else {
            System.err.println("[LocalWebServer] ÉCHEC CRITIQUE : Aucun port disponible entre 8080 et 8099.");
        }
    }

    public synchronized void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
            started = false;
            System.out.println("[LocalWebServer] Serveur arrete.");
        }
    }

    public String getServerBaseUrl() {
        return "http://" + localIp + ":" + port;
    }

    private String getLocalIpAddress() {
        String bestIp = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                String name = ni.getDisplayName().toLowerCase();

                if (ni.isLoopback() || !ni.isUp() || ni.isVirtual()
                        || name.contains("virtual") || name.contains("vmware")
                        || name.contains("vbox") || name.contains("virtualbox")
                        || name.contains("host-only") || name.contains("hyper-v")) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String ip = addr.getHostAddress();
                    if (ip.contains(":")) {
                        continue;
                    }

                    if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                        return ip;
                    }
                    bestIp = ip;
                }
            }
        } catch (Exception ignored) {
        }
        return bestIp;
    }

    private class DocumentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);

            int id = -1;
            try {
                if (params.containsKey("id")) {
                    id = Integer.parseInt(params.get("id"));
                }
            } catch (Exception ignored) {
            }

            DocumentService documentService = new DocumentService();
            Document document = documentService.getById(id);
            String response;

            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            if (document == null) {
                response = "<h1>Document introuvable</h1>";
                exchange.sendResponseHeaders(404, response.getBytes(StandardCharsets.UTF_8).length);
            } else {
                response = buildMobileHtml(document);
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            }

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> map = new HashMap<>();
            if (query != null && !query.isBlank()) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf('=');
                    if (idx > 0) {
                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                        map.put(key, value);
                    }
                }
            }
            return map;
        }

        private String buildMobileHtml(Document document) {
            String title = html(safe(document.getTitre(), "Sans titre"));
            String type = html(safe(document.getTypeDocument(), "N/A"));
            String content = html(safe(document.getContenu(), "Aucun contenu."));
            String date = document.getDateDocument() != null
                    ? html(document.getDateDocument().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    : "N/A";
            String filePath = html(safe(document.getFichierPath(), "Aucun fichier"));

            return "<!DOCTYPE html>"
                    + "<html lang='fr'>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>"
                    + "<title>" + title + " - DocBook</title>"
                    + "<style>"
                    + "body{margin:0;background:#0b1020;color:#e5e7eb;font-family:'Segoe UI',Roboto,Helvetica,sans-serif;}"
                    + ".hero{padding:24px;background:linear-gradient(135deg,#2563eb,#7c3aed);}"
                    + ".hero h1{margin:0;font-size:26px;color:white;}"
                    + ".hero p{margin:8px 0 0;color:#dbeafe;text-transform:uppercase;font-weight:600;letter-spacing:.5px;}"
                    + ".content{padding:20px;}"
                    + ".card{background:#111827;border:1px solid rgba(255,255,255,.08);border-radius:12px;padding:14px;margin-bottom:14px;}"
                    + ".label{font-size:12px;color:#9ca3af;text-transform:uppercase;margin:0 0 6px;}"
                    + ".value{margin:0;font-size:16px;color:#f9fafb;line-height:1.45;word-break:break-word;}"
                    + ".footer{text-align:center;color:#6b7280;font-size:12px;padding:16px;}"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='hero'><h1>" + title + "</h1><p>" + type + "</p></div>"
                    + "<div class='content'>"
                    + "<div class='card'><p class='label'>Date</p><p class='value'>" + date + "</p></div>"
                    + "<div class='card'><p class='label'>Contenu</p><p class='value'>" + content + "</p></div>"
                    + "<div class='card'><p class='label'>Fichier</p><p class='value'>" + filePath + "</p></div>"
                    + "</div>"
                    + "<div class='footer'>DocBook Mobile View</div>"
                    + "</body>"
                    + "</html>";
        }

        private String html(String value) {
            return value
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
        }

        private String safe(String value, String fallback) {
            return value == null || value.isBlank() ? fallback : value;
        }
    }

    private class PatientHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            System.out.println("[WebServer] Requête reçue: " + path + (query != null ? "?" + query : ""));

            Map<String, String> params = parseQuery(query);

            int id = -1;
            try {
                if (params.containsKey("id")) {
                    id = Integer.parseInt(params.get("id"));
                }
            } catch (Exception e) {
                System.err.println("[WebServer] Erreur parsing ID: " + e.getMessage());
            }

            System.out.println("[WebServer] Recherche du dossier ID: " + id);

            DossierMedicalService dossierService = new DossierMedicalService();
            DocumentService documentService = new DocumentService();

            DossierMedical dossier = dossierService.getById(id);
            String response;

            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            if (dossier == null) {
                System.err.println("[WebServer] Patient introuvable pour l'ID: " + id);
                response = "<h1>Dossier introuvable (ID: " + id
                        + ")</h1><p>Vérifiez que le patient existe en base.</p>";
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            List<Document> docs = documentService.getByDossierId(id);
            System.out.println(
                    "[WebServer] Dossier trouvé: " + dossier.getPatientNom() + " (" + docs.size() + " documents)");
            response = buildPatientMobileHtml(dossier, docs);
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String buildPatientMobileHtml(DossierMedical dossier, List<Document> documents) {
            String name = html(dossier.getPatientPrenom() + " " + dossier.getPatientNom());
            String num = html(dossier.getNumeroDossier());
            String remarks = html(safe(dossier.getRemarques(), "Aucune remarque."));
            String contact = html(dossier.getEmail() + " | " + dossier.getTelephone());

            StringBuilder docsHtml = new StringBuilder();
            if (documents.isEmpty()) {
                docsHtml.append("<p class='value'>Aucun document enregistré.</p>");
            } else {
                for (Document doc : documents) {
                    docsHtml.append("<div class='doc-item'>")
                            .append("<strong>").append(html(doc.getTitre())).append("</strong><br>")
                            .append("<small>").append(html(doc.getTypeDocument())).append(" - ")
                            .append(doc.getDateDocument()).append("</small>")
                            .append("</div>");
                }
            }

            return "<!DOCTYPE html>"
                    + "<html lang='fr'>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>"
                    + "<title>" + name + " - DocBook</title>"
                    + "<style>"
                    + "body{margin:0;background:#0f172a;color:#f1f5f9;font-family:system-ui,-apple-system,sans-serif;}"
                    + ".hero{padding:30px 20px;background:linear-gradient(135deg,#3b82f6,#1d4ed8);text-align:center;}"
                    + ".hero h1{margin:0;font-size:24px;}"
                    + ".hero p{margin:5px 0 0;opacity:0.9;font-size:14px;}"
                    + ".content{padding:20px;}"
                    + ".section-title{font-size:13px;color:#94a3b8;text-transform:uppercase;margin:20px 0 10px;font-weight:700;}"
                    + ".card{background:#1e293b;border-radius:16px;padding:20px;box-shadow:0 4px 6px -1px rgba(0,0,0,0.1);margin-bottom:15px;}"
                    + ".value{margin:0;font-size:15px;line-height:1.6;}"
                    + ".doc-item{padding:12px 0;border-bottom:1px solid #334155;}"
                    + ".doc-item:last-child{border-bottom:none;}"
                    + ".doc-item strong{color:#60a5fa;}"
                    + ".footer{text-align:center;color:#64748b;font-size:11px;padding:30px;}"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='hero'><h1>" + name + "</h1><p>Dossier N° " + num + "</p></div>"
                    + "<div class='content'>"
                    + "<div class='section-title'>Contact</div><div class='card'><p class='value'>" + contact
                    + "</p></div>"
                    + "<div class='section-title'>Remarques & Antécédents</div><div class='card'><p class='value'>"
                    + remarks + "</p></div>"
                    + "<div class='section-title'>Documents & Ordonnances</div><div class='card'>" + docsHtml.toString()
                    + "</div>"
                    + "</div>"
                    + "<div class='footer'>DocBook Medical Gateway</div>"
                    + "</body>"
                    + "</html>";
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> map = new HashMap<>();
            if (query != null && !query.isBlank()) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf('=');
                    if (idx > 0) {
                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                        map.put(key, value);
                    }
                }
            }
            return map;
        }

        private String html(String value) {
            if (value == null)
                return "";
            return value
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
        }

        private String safe(String value, String fallback) {
            return value == null || value.isBlank() ? fallback : value;
        }
    }
}