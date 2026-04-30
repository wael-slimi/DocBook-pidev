package org.docbook.entities.appointement;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;
import tn.esprit.services.GeminiService;
import tn.esprit.services.ServiceAppointement;

import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;

/**
 * HomeController - Dashboard with AI Chat, Weather Alerts, and Health Insights
 * 
 * Features:
 * - Real-time weather monitoring with health alerts
 * - Integrated Gemini AI assistant chat window
 * - Appointment statistics and quick actions
 * - Platform.runLater() for all UI updates from background threads
 */
public class HomeController implements Initializable {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label weatherLabel;
    @FXML
    private Label heatwaveAlertLabel;
    @FXML
    private Label appointmentStatsLabel;
    @FXML
    private Button btnOpenChat;
    @FXML
    private Button btnRefreshWeather;
    @FXML
    private VBox dashboardContainer;

    private GeminiService geminiService;
    private ServiceAppointement serviceAppointement;
    private double currentTemperature = 20.0;
    private static final double HEATWAVE_THRESHOLD = 30.0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("🏥 HomeController initializing...");

        geminiService = new GeminiService();
        serviceAppointement = new ServiceAppointement();

        setupUI();
        loadDashboardData();
        checkWeatherAlerts();
    }

    /**
     * Setup initial UI components
     */
    private void setupUI() {
        // Welcome message with greeting based on time of day
        String greeting = getTimeBasedGreeting();
        welcomeLabel.setText(greeting + "Welcome to Medilab Healthcare Dashboard");
        welcomeLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Weather display setup
        weatherLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #2196F3; -fx-font-weight: bold;");

        // Heatwave alert setup (initially hidden)
        heatwaveAlertLabel.setStyle(
                "-fx-font-size: 12; " +
                        "-fx-padding: 12; " +
                        "-fx-background-color: #ffebee; " +
                        "-fx-text-fill: #c62828; " +
                        "-fx-border-color: #f44336; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 4;");
        heatwaveAlertLabel.setVisible(false);
        heatwaveAlertLabel.setManaged(false);
        heatwaveAlertLabel.setWrapText(true);

        // Appointment stats
        appointmentStatsLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

        // Chat button styling
        btnOpenChat.setStyle(
                "-fx-padding: 10 20; " +
                        "-fx-font-size: 12; " +
                        "-fx-background-color: #4CAF50; " +
                        "-fx-text-fill: white; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-radius: 4;");
        btnOpenChat.setOnAction(e -> openChatWindow());

        // Refresh weather button
        btnRefreshWeather.setStyle(
                "-fx-padding: 8 16; " +
                        "-fx-font-size: 11; " +
                        "-fx-background-color: #2196F3; " +
                        "-fx-text-fill: white; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-radius: 4;");
        btnRefreshWeather.setOnAction(e -> {
            checkWeatherAlerts();
            showToast("🔄 Weather Updated", "Fetching latest weather data...");
        });
    }

    /**
     * Load dashboard data asynchronously
     */
    private void loadDashboardData() {
        new Thread(() -> {
            try {
                int totalAppointments = serviceAppointement.readAll().size();
                Platform.runLater(() -> {
                    appointmentStatsLabel.setText(
                            "📊 Total Appointments: " + totalAppointments + " | " +
                                    "🕐 Current Time: " + LocalTime.now().format(
                                            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));

                    // Update Gemini service context
                    geminiService.setTotalAppointments(totalAppointments);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    appointmentStatsLabel.setText("⚠️ Error loading appointment data: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Check weather and trigger health alerts if necessary
     */
    private void checkWeatherAlerts() {
        new Thread(() -> {
            try {
                // Simulate weather API call (replace with actual Weather API)
                currentTemperature = getWeatherTemperature();

                Platform.runLater(() -> {
                    // Update weather display
                    weatherLabel.setText("🌡️ Temperature: " + currentTemperature + "°C");
                    geminiService.setCurrentWeather(currentTemperature + "°C");

                    // Check for heatwave alert
                    if (currentTemperature > HEATWAVE_THRESHOLD) {
                        showHeatwaveAlert();
                    } else {
                        hideHeatwaveAlert();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    weatherLabel.setText("⚠️ Unable to fetch weather");
                    System.err.println("Error fetching weather: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Display heatwave health alert
     */
    private void showHeatwaveAlert() {
        String alertMessage = "🌡️ HEATWAVE ALERT!\n\n" +
                "Temperature is " + currentTemperature + "°C. " +
                "Vulnerable patients (elderly, pregnant, chronic conditions) should:\n" +
                "• Stay hydrated\n" +
                "• Avoid prolonged sun exposure\n" +
                "• Monitor for heat exhaustion\n" +
                "• Contact support if experiencing symptoms";

        heatwaveAlertLabel.setText(alertMessage);
        heatwaveAlertLabel.setVisible(true);
        heatwaveAlertLabel.setManaged(true);

        // Also show toast notification
        showToast("⚠️ Heatwave Alert", "Temperature exceeds 30°C. Check dashboard for details.");
    }

    /**
     * Hide heatwave alert
     */
    private void hideHeatwaveAlert() {
        heatwaveAlertLabel.setVisible(false);
        heatwaveAlertLabel.setManaged(false);
    }

    /**
     * Open the AI Chat window
     */
    @FXML
    private void openChatWindow() {
        Stage chatStage = new Stage();
        chatStage.setTitle("🤖 Medilab AI Assistant");
        chatStage.setWidth(600);
        chatStage.setHeight(700);

        // Main chat container
        VBox chatContainer = new VBox(10);
        chatContainer.setPadding(new Insets(15));
        chatContainer.setStyle("-fx-background-color: #ffffff;");

        // Chat history area
        TextArea chatHistory = new TextArea();
        chatHistory.setEditable(false);
        chatHistory.setWrapText(true);
        chatHistory
                .setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11; -fx-control-inner-background: #f5f5f5;");
        chatHistory.setText("🤖 Medilab AI Assistant\n\nHello! I'm your healthcare assistant. Ask me about:\n" +
                "• Appointment management\n• Appointment schedules\n• Medical information\n• Clinic navigation\n\n");

        // Input area
        HBox inputBox = new HBox(8);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        TextField messageInput = new TextField();
        messageInput.setPromptText("Type your question here...");
        messageInput.setStyle("-fx-font-size: 12; -fx-padding: 8;");

        Button sendButton = new Button("Send");
        sendButton.setStyle(
                "-fx-padding: 8 16; " +
                        "-fx-font-size: 11; " +
                        "-fx-background-color: #4CAF50; " +
                        "-fx-text-fill: white; " +
                        "-fx-cursor: hand;");

        sendButton.setOnAction(e -> {
            String userMessage = messageInput.getText().trim();
            if (!userMessage.isEmpty()) {
                // Display user message
                chatHistory.appendText("\n👤 You: " + userMessage + "\n");
                messageInput.clear();

                // Show typing indicator
                chatHistory.appendText("🤖 Medilab: Processing...\n");

                // Send to Gemini service with callback
                geminiService.sendMessage(userMessage, response -> {
                    // Remove "Processing..." and add actual response
                    String currentText = chatHistory.getText();
                    if (currentText.endsWith("🤖 Medilab: Processing...\n")) {
                        chatHistory.setText(currentText.substring(0,
                                currentText.length() - "🤖 Medilab: Processing...\n".length()));
                    }
                    chatHistory.appendText("🤖 Medilab: " + response + "\n\n");
                    chatHistory.setScrollTop(Double.MAX_VALUE); // Auto-scroll to bottom
                });
            }
        });

        // Allow Enter key to send message
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                sendButton.fire();
            }
        });

        inputBox.getChildren().addAll(messageInput, sendButton);
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        // Add components to container
        chatContainer.getChildren().addAll(
                new Label("💬 Chat History"),
                chatHistory,
                new Separator(),
                inputBox);
        VBox.setVgrow(chatHistory, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(chatContainer);
        scrollPane.setFitToWidth(true);

        chatStage.setScene(new javafx.scene.Scene(scrollPane));
        chatStage.show();
    }

    /**
     * Show toast notification using ControlsFX
     */
    private void showToast(String title, String message) {
        Platform.runLater(() -> {
            Notifications.create()
                    .title(title)
                    .text(message)
                    .showInformation();
        });
    }

    /**
     * Get time-based greeting
     */
    private String getTimeBasedGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) {
            return "☀️ Good Morning! ";
        } else if (hour < 17) {
            return "☀️ Good Afternoon! ";
        } else {
            return "🌙 Good Evening! ";
        }
    }

    /**
     * Simulate weather API call
     * Replace with actual Weather API integration
     */
    private double getWeatherTemperature() {
        // Simulate temperature fetch
        // In production, integrate with a weather API like OpenWeatherMap
        try {
            Thread.sleep(500); // Simulate network delay
            return Math.random() * 35 + 5; // Random temp between 5-40°C
        } catch (InterruptedException e) {
            return 20.0; // Default
        }
    }
}
