package org.docbook.controllers.patient;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.docbook.models.ChatMessage;
import org.docbook.services.SimpleChatService;

public class ChatWidget extends VBox {

    private VBox chatMessagesContainer;
    private TextField messageInputField;
    private Label statusLabel;
    private SimpleChatService chatService;
    private boolean isWaiting = false;

    public ChatWidget() {
        chatService = new SimpleChatService();
        initialize();
    }

    private void initialize() {
        setSpacing(0);
        setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");
        
        Label header = new Label("\uD83E\uDD14 DocBook Assistant");
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 10;");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER_LEFT);
        
        chatMessagesContainer = new VBox(8);
        chatMessagesContainer.setPadding(new Insets(10));
        chatMessagesContainer.setStyle("-fx-background-color: #f5f5f5;");
        
        ScrollPane scrollPane = new ScrollPane(chatMessagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setPrefHeight(200);
        
        HBox inputBox = new HBox(8);
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-background-color: #fff; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        
        messageInputField = new TextField();
        messageInputField.setPromptText("Ask me anything...");
        messageInputField.setStyle("-fx-font-size: 12px; -fx-padding: 8;");
        HBox.setHgrow(messageInputField, Priority.ALWAYS);
        
        Button sendBtn = new Button("\u27A4");
        sendBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #0058be; -fx-text-fill: white; -fx-padding: 8 12; -fx-cursor: hand;");
        sendBtn.setOnAction(e -> sendMessage());
        
        inputBox.getChildren().addAll(messageInputField, sendBtn);
        
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666; -fx-padding: 5 10;");
        
        messageInputField.setOnAction(e -> sendMessage());
        
        getChildren().addAll(header, scrollPane, inputBox, statusLabel);
    }

    private void sendMessage() {
        String userInput = messageInputField.getText().trim();
        
        if (userInput.isEmpty()) {
            showStatus("\u26A0 Please enter a message", "#f39c12");
            return;
        }
        
        if (isWaiting) {
            showStatus("\u23F3 Waiting...", "#3498db");
            return;
        }
        
        displayUserMessage(userInput);
        messageInputField.clear();
        
        isWaiting = true;
        showStatus("\u23F3 AI is thinking...", "#3498db");
        
        chatService.sendMessageAsync(userInput, new SimpleChatService.ChatServiceCallback() {
            @Override
            public void onSuccess(String response) {
                Platform.runLater(() -> {
                    displayAssistantMessage(response);
                    showStatus("\u2705 Ready", "#27ae60");
                    isWaiting = false;
                });
            }

            @Override
            public void onError(String error) {
                Platform.runLater(() -> {
                    displayAssistantMessage("\u274C Error: " + error);
                    showStatus("\u274C Error", "#e74c3c");
                    isWaiting = false;
                });
            }
        });
    }

    private void displayUserMessage(String message) {
        HBox messageBox = new HBox(10);
        messageBox.setPadding(new Insets(8));
        messageBox.setStyle("-fx-background-color: #e3f2fd; -fx-border-radius: 8;");
        messageBox.setAlignment(Pos.TOP_RIGHT);

        TextArea text = new TextArea(message);
        text.setWrapText(true);
        text.setEditable(false);
        text.setPrefRowCount(2);
        text.setStyle("-fx-font-size: 12px; -fx-control-inner-background: #e3f2fd;");

        messageBox.getChildren().add(text);
        chatMessagesContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void displayAssistantMessage(String message) {
        HBox messageBox = new HBox(10);
        messageBox.setPadding(new Insets(8));
        messageBox.setStyle("-fx-background-color: #f1f8e9; -fx-border-radius: 8;");
        messageBox.setAlignment(Pos.TOP_LEFT);

        Label botIcon = new Label("\uD83E\uDD14");
        botIcon.setStyle("-fx-font-size: 16px;");

        TextArea text = new TextArea(message);
        text.setWrapText(true);
        text.setEditable(false);
        text.setPrefRowCount(3);
        text.setStyle("-fx-font-size: 12px; -fx-control-inner-background: #f1f8e9;");
        HBox.setHgrow(text, Priority.ALWAYS);

        messageBox.getChildren().addAll(botIcon, text);
        chatMessagesContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            ((ScrollPane) chatMessagesContainer.getParent()).setVvalue(1.0);
        });
    }

    private void showStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + color + ";");
    }
}