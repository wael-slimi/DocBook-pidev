package org.docbook.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * WeatherWidget - A reusable JavaFX component that displays current weather
 * Features:
 * - Real-time weather updates from Open-Meteo API
 * - Weather icon with emoji representation
 * - Temperature, condition, and wind speed display
 * - Automatic refresh capability
 * - Professional styling
 */
public class WeatherWidget extends VBox {

    private Label iconLabel;
    private Label locationLabel;
    private Label temperatureLabel;
    private Label conditionLabel;
    private Label windLabel;
    private Label updateTimeLabel;

    public WeatherWidget() {
        initialize();
        loadWeather();
    }

    /**
     * Initialize the UI components
     */
    private void initialize() {
        setSpacing(8);
        setStyle("-fx-padding: 15; -fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);");
        setAlignment(Pos.TOP_CENTER);

        // Weather Icon (Large)
        iconLabel = new Label("🌤️");
        iconLabel.setStyle("-fx-font-size: 32px;");

        // Location Header
        locationLabel = new Label("Tunis, Tunisia");
        locationLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Temperature Display
        temperatureLabel = new Label("-- °C");
        temperatureLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Condition
        conditionLabel = new Label("Loading...");
        conditionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ecf0f1;");

        // Wind Speed
        windLabel = new Label("Wind: --");
        windLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #bdc3c7;");

        // Update Time
        updateTimeLabel = new Label("Last updated: just now");
        updateTimeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #95a5a6;");

        // Add components to layout
        getChildren().addAll(
                iconLabel,
                locationLabel,
                temperatureLabel,
                conditionLabel,
                windLabel,
                updateTimeLabel);
    }

    /**
     * Load weather data asynchronously
     */
    public void loadWeather() {
        WeatherApiClient.fetchWeatherAsync(
                weather -> Platform.runLater(() -> updateUI(weather)),
                error -> Platform.runLater(() -> showError()));
    }

    /**
     * Update UI with weather data
     */
    private void updateUI(WeatherApiClient.WeatherData weather) {
        iconLabel.setText(weather.icon);
        temperatureLabel.setText(String.format("%.0f°C", weather.temperature));
        conditionLabel.setText(weather.condition);
        windLabel.setText(String.format("💨 Wind: %.1f km/h", weather.windSpeed));
        updateTimeLabel.setText("Last updated: just now");
    }

    /**
     * Show error state
     */
    private void showError() {
        temperatureLabel.setText("--°C");
        conditionLabel.setText("Unable to load");
        windLabel.setText("Check connection");
    }

    /**
     * Manual refresh
     */
    public void refresh() {
        loadWeather();
    }
}
