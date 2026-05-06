package org.docbook.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiService {

    private static final String API_KEY = EnvLoader.get("GEMINI_API_KEY", "YOUR_GEMINI_API_KEY_HERE");
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
            + API_KEY;

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String analyzeText(String prompt, String content) {
        if (API_KEY == null || API_KEY.equals("YOUR_GEMINI_API_KEY_HERE")) {
            return "Veuillez configurer votre clé API Gemini dans le fichier .env pour activer l'IA.";
        }

        int maxRetries = 15;
        int retryCount = 0;
        int delay = 3000;

        while (retryCount < maxRetries) {
            try {
                ObjectNode rootNode = objectMapper.createObjectNode();
                ObjectNode contentsNode = rootNode.putArray("contents").addObject();
                ObjectNode partsNode = contentsNode.putArray("parts").addObject();
                partsNode.put("text", prompt + "\n\nDonnées fournies:\n" + content);

                String requestBody = objectMapper.writeValueAsString(rootNode);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode jsonResponse = objectMapper.readTree(response.body());
                    return jsonResponse.path("candidates").get(0)
                            .path("content").path("parts").get(0)
                            .path("text").asText();
                } else if (response.statusCode() == 503 || response.statusCode() == 429) {
                    System.err.println(
                            "Gemini Busy (503/429). Retrying in " + delay + "ms... (Attempt " + (retryCount + 1) + ")");
                    Thread.sleep(delay);
                    retryCount++;
                    delay *= 1.5;
                } else {
                    System.err.println("Gemini API Error: " + response.body());
                    return "Erreur API AI: " + response.statusCode();
                }
            } catch (Exception e) {
                e.printStackTrace();
                retryCount++;
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        return "L'IA est actuellement saturée. Veuillez réessayer dans quelques instants.";
    }

    public static String getSnapshotPrompt() {
        return "Analyse toutes les remarques et documents de ce patient. Génère un 'Snapshot' médical en EXACTEMENT 3 phrases. "
                +
                "La 1ère sur les antécédents, la 2ème sur l'état actuel, la 3ème sur les points de vigilance. Réponds en français.";
    }

    public static String getSafetyGuardPrompt(String patientHistory) {
        return "Tu es un garde-fou médical. Compare la prescription suivante avec les antécédents du patient: "
                + patientHistory +
                ". Si tu détectes une allergie ou une interaction dangereuse, réponds par un message d'ALERTE ROUGE court. "
                +
                "Sinon, réponds 'SÉCURISÉ'. Réponds en français.";
    }

    public static String getAutoTitlePrompt() {
        return "Lis ce compte-rendu médical et suggère UNIQUEMENT un titre professionnel court (max 5 mots) et le type de document "
                +
                "(Consultation, Ordonnance, Imagerie, ou Analyse). Format: Titre | Type";
    }

    public static String getDiagnosticAssistantPrompt() {
        return "En tant que co-pilote médical, analyse ces symptômes. Suggère 2 questions clés à poser au patient et 1 examen complémentaire pertinent. Réponds de manière très concise en français.";
    }

    public static String getPatientFriendlyPrompt() {
        return "Réécris ce compte-rendu médical technique en langage 'Patient-Friendly'. " +
                "Explique les choses simplement, sans jargon, de manière rassurante mais honnête. Réponds en français.";
    }
}