package org.docbook.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.HttpURLConnection;

public class WeatherApiClient {

    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast?" +
            "latitude=36.8065&longitude=10.1815&" +
            "current=temperature_2m,weather_code,wind_speed_10m&" +
            "timezone=Africa/Tunis";

    private static final int[] SUNNY_CODES = { 0, 1 };
    private static final int[] CLOUDY_CODES = { 2, 3, 45, 48 };
    private static final int[] RAINY_CODES = { 51, 53, 55, 61, 63, 65, 80, 81, 82 };
    private static final int[] STORMY_CODES = { 80, 81, 82, 85, 86 };

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
            return String.format("%.0f\u00B0C \u2022 %s \u2022 Wind: %.1f km/h", temperature, condition, windSpeed);
        }
    }

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

    public static WeatherData fetchWeather() throws IOException {
        StringBuilder response = new StringBuilder();
        
        HttpURLConnection connection = (HttpURLConnection) URI.create(WEATHER_API_URL).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        String json = response.toString();
        
        double temperature = extractValue(json, "temperature_2m");
        int weatherCode = (int) extractValue(json, "weather_code");
        double windSpeed = extractValue(json, "wind_speed_10m");

        String condition = getWeatherCondition(weatherCode);
        String icon = getWeatherIcon(weatherCode);

        return new WeatherData(temperature, condition, icon, windSpeed);
    }

    private static double extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int index = json.indexOf(searchKey);
        if (index == -1) return 0;
        
        int start = index + searchKey.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        
        String value = json.substring(start, end).trim();
        return Double.parseDouble(value);
    }

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

    private static String getWeatherIcon(int code) {
        if (contains(SUNNY_CODES, code)) {
            return "\u2600\uFE0F";
        } else if (contains(CLOUDY_CODES, code)) {
            return "\u2601\uFE0F";
        } else if (contains(RAINY_CODES, code)) {
            return "\uD83C\uDF27\uFE0F";
        } else if (contains(STORMY_CODES, code)) {
            return "\u26C8\uFE0F";
        }
        return "\uD83C\uDF24\uFE0F";
    }

    private static boolean contains(int[] array, int value) {
        for (int i : array) {
            if (i == value) return true;
        }
        return false;
    }

    public interface WeatherCallback {
        void onSuccess(WeatherData weather);
    }

    public interface ErrorCallback {
        void onError(Exception e);
    }
}