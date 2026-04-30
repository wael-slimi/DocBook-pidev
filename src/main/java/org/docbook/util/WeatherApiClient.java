package org.docbook.util;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.Scanner;

/**
 * WeatherApiClient - Handles integration with Open-Meteo Weather API
 * Provides real-time weather data for Tunisia (Tunis)
 *
 * Uses Open-Meteo API (free, no API key required)
 * Coordinates: Tunis (36.8065°N, 10.1815°E)
 */
public class WeatherApiClient {

    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast?" +
            "latitude=36.8065&longitude=10.1815&" +
            "current=temperature_2m,weather_code,wind_speed_10m&" +
            "timezone=Africa/Tunis";

    private static final int[] SUNNY_CODES = { 0, 1 };
    private static final int[] CLOUDY_CODES = { 2, 3, 45, 48 };
    private static final int[] RAINY_CODES = { 51, 53, 55, 61, 63, 65, 80, 81, 82 };
    private static final int[] STORMY_CODES = { 80, 81, 82, 85, 86 };

    /**
     * Weather data holder class
     */
    public static class WeatherData {
        public double temperature;
        public String condition;
        public String icon;
        public double windSpeed;
        public long timestamp;

        public WeatherData(double temperature, String condition, String icon, double windSpeed) {
            this.temperature = temperature;
            this.condition = condition;
            this.icon = icon;
            this.windSpeed = windSpeed;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("%.0f°C • %s • Wind: %.1f km/h", temperature, condition, windSpeed);
        }
    }

    /**
     * Fetch current weather for Tunis
     * Uses background thread to avoid blocking UI
     */
    public static void fetchWeatherAsync(WeatherCallback callback, ErrorCallback errorCallback) {
        new Thread(() -> {
            try {
                WeatherData weather = fetchWeather();
                callback.onSuccess(weather);
            } catch (Exception e) {
                System.err.println("Error fetching weather: " + e.getMessage());
                errorCallback.onError(e);
            }
        }).start();
    }

    /**
     * Synchronously fetch weather data
     */
    public static WeatherData fetchWeather() throws IOException {
        // Use URI.create().toURL() instead of deprecated new URL(String)
        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(URI.create(WEATHER_API_URL).toURL().openStream())) {
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
        }

        JSONObject json = new JSONObject(response.toString());
        JSONObject current = json.getJSONObject("current");

        double temperature = current.getDouble("temperature_2m");
        int weatherCode = current.getInt("weather_code");
        double windSpeed = current.getDouble("wind_speed_10m");

        String condition = getWeatherCondition(weatherCode);
        String icon = getWeatherIcon(weatherCode);

        return new WeatherData(temperature, condition, icon, windSpeed);
    }

    /**
     * Convert WMO weather codes to readable conditions
     */
    private static String getWeatherCondition(int code) {
        if (contains(SUNNY_CODES, code)) {
            return "Sunny";
        } else if (contains(CLOUDY_CODES, code)) {
            return "Cloudy";
        } else if (contains(RAINY_CODES, code)) {
            return "Rainy";
        } else if (contains(STORMY_CODES, code)) {
            return "Stormy";
        }
        return "Clear";
    }

    /**
     * Map weather codes to emoji icons
     */
    private static String getWeatherIcon(int code) {
        if (contains(SUNNY_CODES, code)) {
            return "☀️";
        } else if (contains(CLOUDY_CODES, code)) {
            return "☁️";
        } else if (contains(RAINY_CODES, code)) {
            return "🌧️";
        } else if (contains(STORMY_CODES, code)) {
            return "⛈️";
        }
        return "🌤️";
    }

    private static boolean contains(int[] array, int value) {
        for (int i : array) {
            if (i == value)
                return true;
        }
        return false;
    }

    /**
     * Callback interface for async weather fetching
     */
    public interface WeatherCallback {
        void onSuccess(WeatherData weather);
    }

    /**
     * Error callback interface
     */
    public interface ErrorCallback {
        void onError(Exception e);
    }
}