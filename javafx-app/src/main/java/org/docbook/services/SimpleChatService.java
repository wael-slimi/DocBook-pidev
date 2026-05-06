package org.docbook.services;

import java.util.*;

import org.docbook.models.ChatMessage;

public class SimpleChatService {

    private final List<ChatMessage> chatHistory = new ArrayList<>();
    private static final int MAX_HISTORY = 50;

    private static final Map<String, String> RESPONSES = new HashMap<>();
    
    static {
        RESPONSES.put("hello", "Hello! I'm your DocBook Assistant. How can I help you today?");
        RESPONSES.put("hi", "Hi there! How can I assist you?");
        RESPONSES.put("hey", "Hey! What can I help you with?");
        
        RESPONSES.put("appointment", "To book an appointment, go to the Rendez-vous section and click '+ Nouveau'. You can select a doctor and date that works for you.");
        
        RESPONSES.put("rendez", "Pour demander un rendez-vous, allez dans la section Rendez-vous et cliquez sur '+ Nouveau'.");
        
        RESPONSES.put("rdv", "Vous pouvez demander un rendez-vous en cliquant sur le bouton '+ Nouveau' dans la section Rendez-vous.");
        
        RESPONSES.put("doctor", "You can view available doctors when booking an appointment. They are listed by department and specialty.");
        
        RESPONSES.put("consultation", "Teleconsultations allow you to meet with doctors remotely via video call. Your doctor will create the session.");
        
        RESPONSES.put("teleconsultation", "To have a teleconsultation, your doctor must first create a session after confirming your appointment.");
        
        RESPONSES.put("help", "I can help you with:\n- Booking appointments\n- Understanding teleconsultations\n- Finding doctors\n- Using the app\n\nJust ask!");
        
        RESPONSES.put("aide", "Je peux vous aider avec:\n- Demander un rendez-vous\n- Comprendre les téléconsultations\n- Trouver un médecin\n\nPosez-moi votre question!");
        
        RESPONSES.put("thank", "You're welcome! Happy to help!");
        RESPONSES.put("merci", "De rien! Bonne journée!");
        
        RESPONSES.put("cancel", "To cancel an appointment, go to the Rendez-vous section and click 'Delete' on the appointment card.");
        
        RESPONSES.put("emergency", "For medical emergencies, please call your local emergency number immediately. This app is for regular consultations only.");
        
        RESPONSES.put("urgence", "Pour une urgence médicale, appelez immédiatement le 190. Cette application est pour les consultations régulières.");
        
        RESPONSES.put("hours", "Our clinic is open Monday to Friday from 8:00 AM to 6:00 PM.");
        
        RESPONSES.put("horaire", "La clinique est ouverte du lundi au vendredi de 8h à 18h.");
        
        RESPONSES.put("contact", "You can contact us through the app or call our reception during business hours.");
    }

    public void sendMessageAsync(String userMessage, ChatServiceCallback callback) {
        new Thread(() -> {
            try {
                ChatMessage userMsg = new ChatMessage(userMessage, ChatMessage.MessageType.USER);
                chatHistory.add(userMsg);

                String response = getResponse(userMessage);

                ChatMessage assistantMsg = new ChatMessage(response, ChatMessage.MessageType.ASSISTANT);
                chatHistory.add(assistantMsg);

                if (chatHistory.size() > MAX_HISTORY) {
                    chatHistory.subList(chatHistory.size() - MAX_HISTORY, chatHistory.size());
                }

                callback.onSuccess(response);
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    private String getResponse(String input) {
        String lowerInput = input.toLowerCase().trim();
        
        for (Map.Entry<String, String> entry : RESPONSES.entrySet()) {
            if (lowerInput.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return "I'm here to help! Try asking about:\n- Appointments (rendez-vous)\n- Doctors\n- Teleconsultations\n- Emergency numbers\n\nOr type 'help' for more options.";
    }

    public List<ChatMessage> getChatHistory() {
        return new ArrayList<>(chatHistory);
    }

    public void clearHistory() {
        chatHistory.clear();
    }

    public interface ChatServiceCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}