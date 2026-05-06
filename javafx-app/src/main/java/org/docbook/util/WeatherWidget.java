package org.docbook.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class WeatherWidget extends VBox {

    private Label iconLabel;
    private Label locationLabel;
    private Label temperatureLabel;
    private Label conditionLabel;
    private Label windLabel;

    public WeatherWidget() {
        initialize();
        loadWeather();
    }

    private void initialize() {
        setSpacing(8);
        setStyle("-fx-padding: 15; -fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);");
        setAlignment(Pos.TOP_CENTER);

        iconLabel = new Label("\uD83C\uDF24\uFE0F");
        iconLabel.setStyle("-fx-font-size: 32px;");

        locationLabel = new Label("Tunis, Tunisia");
        locationLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        temperatureLabel = new Label("-- \u00B0C");
        temperatureLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        conditionLabel = new Label("Loading...");
        conditionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ecf0f1;");

        windLabel = new Label("Wind: --");
        windLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #bdc3c7;");

        getChildren().addAll(iconLabel, locationLabel, temperatureLabel, conditionLabel, windLabel);
    }

    public void loadWeather() {
        WeatherApiClient.fetchWeatherAsync(
                weather -> Platform.runLater(() -> updateUI(weather)),
                error -> Platform.runLater(() -> showError()));
    }

    private void updateUI(WeatherApiClient.WeatherData weather) {
        iconLabel.setText(weather.icon);
        temperatureLabel.setText(String.format("%.0f\u00B0C", weather.temperature));
        conditionLabel.setText(weather.condition);
        windLabel.setText(String.format("\uD83D\uDCA8 Wind: %.1f km/h", weather.windSpeed));
    }

    private void showError() {
        temperatureLabel.setText("--\u00B0C");
        conditionLabel.setText("Unable to load");
        windLabel.setText("Check connection");
    }

    public void refresh() {
        loadWeather();
    }
}