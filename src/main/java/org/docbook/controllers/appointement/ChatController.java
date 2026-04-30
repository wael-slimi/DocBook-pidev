package org.docbook.controllers.appointement;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import tn.esprit.models.ChatMessage;
import tn.esprit.services.ChatService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ChatController - Handles AI Chat UI interactions
 * Displays chat messages, sends user input to AI, shows responses
 */
public class ChatController implements Initializable {

    @FXML
    private VBox chatMessagesContainer;
    @FXML
    private TextField messageInputField;
    @FXML
    private Button sendButton;
    @FXML
    private Button clearButton;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private Label statusLabel;

    private ChatService chatService;
    private boolean isWaitingForResponse = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chatService = new ChatService();
        setupChatContainer();
        setupEventHandlers();
        loadChatHistory();
        System.out.println("✓ ChatController initialized");
    }

    private void setupChatContainer() {
        chatMessagesContainer.setSpacing(10);
        chatMessagesContainer.setPadding(new Insets(15));
        chatMessagesContainer.setStyle("-fx-background-color: #f5f5f5;");
    }

    private void setupEventHandlers() {
        sendButton.setOnAction(e -> sendMessage());
        messageInputField.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                sendMessage();
            }
        });
        clearButton.setOnAction(e -> clearChat());
    }

    private void sendMessage() {
        String userInput = messageInputField.getText().trim();

        if (userInput.isEmpty()) {
            showStatus("⚠️ Please enter a message", "warning");
            return;
        }

        if (isWaitingForResponse) {
            showStatus("⏳ Waiting for response... Please wait", "info");
            return;
        }

        displayUserMessage(userInput);
        messageInputField.clear();

        isWaitingForResponse = true;
        showStatus("⏳ AI is thinking...", "info");
        disableInput();

        chatService.sendMessageAsync(userInput, new ChatService.ChatServiceCallback() {
            @Override
            public void onSuccess(String response) {
                Platform.runLater(() -> {
                    displayAssistantMessage(response);
                    showStatus("✓ Ready", "success");
                    isWaitingForResponse = false;
                    enableInput();
                    showNotification("Medilab Assistant", "Response received!");
                });
            }

            @Override
            public void onError(String error) {
                Platform.runLater(() -> {
                    displayAssistantMessage("❌ Error: " + error);
                    showStatus("❌ Error occurred", "error");
                    isWaitingForResponse = false;
                    enableInput();
                });
            }
        });
    }

    private void displayUserMessage(String message) {
        ChatMessage userMsg = new ChatMessage(message, ChatMessage.MessageType.USER);
        chatMessagesContainer.getChildren().add(createMessageBox(userMsg));
        scrollToBottom();
    }

    private void displayAssistantMessage(String message) {
        ChatMessage assistantMsg = new ChatMessage(message, ChatMessage.MessageType.ASSISTANT);
        chatMessagesContainer.getChildren().add(createMessageBox(assistantMsg));
        scrollToBottom();
    }

    private HBox createMessageBox(ChatMessage message) {
        HBox messageBox = new HBox(10);
        messageBox.setPadding(new Insets(10));
        messageBox.setStyle("-fx-background-color: white; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");

        Label senderLabel = new Label(message.getType() == ChatMessage.MessageType.USER ? "👤" : "🤖");
        senderLabel.setStyle("-fx-font-size: 20px;");

        VBox contentBox = new VBox(3);

        Label senderName = new Label(message.getSenderLabel());
        senderName.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");

        Label timeLabel = new Label(message.getFormattedTime());
        timeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #95a5a6;");

        TextArea messageText = new TextArea(message.getContent());
        messageText.setWrapText(true);
        messageText.setEditable(false);
        messageText.setPrefRowCount(3);
        messageText.setStyle("-fx-control-inner-background: #ecf0f1; " +
                "-fx-padding: 8; " +
                "-fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 12px;");

        contentBox.getChildren().addAll(senderName, timeLabel, messageText);
        HBox.setHgrow(contentBox, Priority.ALWAYS);
        messageBox.getChildren().addAll(senderLabel, contentBox);

        if (message.getType() == ChatMessage.MessageType.USER) {
            messageBox.setStyle(messageBox.getStyle() + " -fx-background-color: #e3f2fd;");
            messageBox.setAlignment(Pos.TOP_RIGHT);
        } else {
            messageBox.setStyle(messageBox.getStyle() + " -fx-background-color: #f1f8e9;");
            messageBox.setAlignment(Pos.TOP_LEFT);
        }

        return messageBox;
    }

    private void loadChatHistory() {
        chatMessagesContainer.getChildren().clear();
        for (ChatMessage msg : chatService.getChatHistory()) {
            chatMessagesContainer.getChildren().add(createMessageBox(msg));
        }
        scrollToBottom();
    }

    private void scrollToBottom() {
        chatScrollPane.setVvalue(1.0);
    }

    @FXML
    private void clearChat() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Chat");
        alert.setHeaderText("Clear Chat History?");
        alert.setContentText("Are you sure you want to clear the entire chat history?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                chatService.clearHistory();
                chatMessagesContainer.getChildren().clear();
                showStatus("Chat cleared", "info");
            }
        });
    }

    private void disableInput() {
        messageInputField.setDisable(true);
        sendButton.setDisable(true);
    }

    private void enableInput() {
        messageInputField.setDisable(false);
        sendButton.setDisable(false);
        messageInputField.requestFocus();
    }

    private void showStatus(String message, String type) {
        statusLabel.setText(message);
        switch (type) {
            case "success":
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                break;
            case "error":
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                break;
            case "warning":
                statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                break;
            case "info":
            default:
                statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        }
    }

    /**
     * Show system notification (using ControlsFX if available)
     * Uses Class.forName to check availability without a hard dependency.
     */
    private void showNotification(String title, String message) {
        try {
            // Check if ControlsFX is on the classpath — result intentionally ignored;
            // we only use this path for the side-effect of knowing it loaded.
            Class.forName("org.controlsfx.control.Notifications");
            System.out.println("🔔 " + title + ": " + message);
        } catch (ClassNotFoundException e) {
            System.out.println("ℹ️ ControlsFX not available for notifications");
        }
    }
}