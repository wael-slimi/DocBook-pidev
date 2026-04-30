package org.docbook.entities.appointement;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * ChatWindowController - Manages the chat window/popup
 * Can be displayed as a separate window or integrated PopOver
 */
public class ChatWindowController {

    private Stage chatStage;
    private Parent chatRoot;

    /**
     * Show chat window as modal dialog
     */
    public void showChatWindow(Button helpButton) {
        try {
            if (chatStage == null) {
                // Load chat.fxml
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/chat.fxml"));
                chatRoot = loader.load();

                // Create stage
                chatStage = new Stage();
                chatStage.setTitle("Medilab AI Assistant");
                chatStage.initModality(Modality.APPLICATION_MODAL);
                chatStage.initStyle(StageStyle.DECORATED);

                Scene scene = new Scene(chatRoot, 600, 700);
                chatStage.setScene(scene);

                // Position window next to help button if possible
                if (helpButton != null) {
                    Bounds bounds = helpButton.localToScreen(helpButton.getBoundsInLocal());
                    chatStage.setX(bounds.getCenterX() - 300);
                    chatStage.setY(bounds.getCenterY());
                }

                // Handle close
                chatStage.setOnCloseRequest(e -> chatStage = null);
            }

            // Show window
            if (!chatStage.isShowing()) {
                chatStage.show();
                chatStage.requestFocus();
            } else {
                chatStage.toFront();
                chatStage.requestFocus();
            }

        } catch (Exception e) {
            System.err.println("❌ Error opening chat window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Close chat window
     */
    public void closeChatWindow() {
        if (chatStage != null && chatStage.isShowing()) {
            chatStage.close();
            chatStage = null;
        }
    }

    /**
     * Check if chat window is open
     */
    public boolean isChatWindowOpen() {
        return chatStage != null && chatStage.isShowing();
    }

    /**
     * Toggle chat window (open if closed, close if open)
     */
    public void toggleChatWindow(Button helpButton) {
        if (isChatWindowOpen()) {
            closeChatWindow();
        } else {
            showChatWindow(helpButton);
        }
    }
}
