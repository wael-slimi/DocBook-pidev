package org.docbook.util;

/**
 * HealthTipService - Provides health tips based on weather conditions
 * Features:
 * - Analyzes current temperature
 * - Generates health alerts for extreme temperatures
 * - Specific recommendations for patient groups
 * - Integration with WeatherApiClient
 */
public class HealthTipService {

    /**
     * Data class for health tip
     */
    public static class HealthTip {
        public String title;
        public String message;
        public String icon;
        public String severity; // LOW, MEDIUM, HIGH
        public String patientGroup; // Who should heed this

        public HealthTip(String title, String message, String icon, String severity, String patientGroup) {
            this.title = title;
            this.message = message;
            this.icon = icon;
            this.severity = severity;
            this.patientGroup = patientGroup;
        }

        @Override
        public String toString() {
            return title + "\n" + message + " (" + patientGroup + ")";
        }
    }

    /**
     * Generate health tip based on temperature
     * Returns relevant health recommendations
     */
    public static HealthTip generateHealthTipFromTemperature(double celsius) {
        if (celsius > 35) {
            return new HealthTip(
                    "🌡️ EXTREME HEAT ALERT",
                    "Temperature exceeds 35°C. Heatwave conditions detected.\n" +
                            "• Cardiovascular patients: Stay in air-conditioned areas\n" +
                            "• Drink 2-3 liters of water daily\n" +
                            "• Avoid strenuous activities during peak heat (11 AM - 4 PM)\n" +
                            "• Check blood pressure regularly",
                    "🔥",
                    "HIGH",
                    "Heart & Hypertension Patients");
        } else if (celsius >= 30) {
            return new HealthTip(
                    "☀️ HOT WEATHER PRECAUTION",
                    "High temperature day (30-35°C). Stay hydrated.\n" +
                            "• Wear light, breathable clothing\n" +
                            "• Elderly: Take extra precautions\n" +
                            "• Diabetes patients: Monitor blood sugar more frequently\n" +
                            "• Stay in shade when possible",
                    "☀️",
                    "MEDIUM",
                    "Elderly & Chronic Patients");
        } else if (celsius < 0) {
            return new HealthTip(
                    "❄️ EXTREME COLD ALERT",
                    "Freezing temperatures below 0°C.\n" +
                            "• Respiratory patients: Avoid outdoor exposure\n" +
                            "• Arthritis sufferers: Expect increased joint pain\n" +
                            "• Heart patients: Risk of hypertension spike in cold\n" +
                            "• Keep indoors as much as possible",
                    "❄️",
                    "HIGH",
                    "Respiratory & Heart Patients");
        } else if (celsius >= 0 && celsius < 5) {
            return new HealthTip(
                    "🧊 COLD WEATHER ALERT",
                    "Cold conditions (0-5°C). Bundle up appropriately.\n" +
                            "• Wear multiple layers\n" +
                            "• Asthma patients: Cold air may trigger symptoms\n" +
                            "• Stay warm and avoid sudden temperature changes\n" +
                            "• Use humidifier indoors",
                    "🧊",
                    "MEDIUM",
                    "Asthma & Respiratory Patients");
        } else if (celsius >= 5 && celsius < 15) {
            return new HealthTip(
                    "🌤️ MILD CONDITIONS",
                    "Comfortable temperature range (5-15°C).\n" +
                            "• Good day for outdoor walking/exercise\n" +
                            "• General health: Maintain regular activities\n" +
                            "• Perfect weather for outdoor consultations if needed\n" +
                            "• Stay hydrated even in cooler weather",
                    "✅",
                    "LOW",
                    "All Patients");
        } else {
            return new HealthTip(
                    "🌡️ NORMAL CONDITIONS",
                    "Temperature in healthy range (15-30°C).\n" +
                            "• Regular health precautions apply\n" +
                            "• Maintain routine medication schedule\n" +
                            "• Stay hydrated\n" +
                            "• Regular exercise recommended",
                    "✓",
                    "LOW",
                    "General Population");
        }
    }

    /**
     * Get weather-based appointment recommendation
     * Suggests if patient should reschedule or prepare specially
     */
    public static String getAppointmentRecommendation(double celsius) {
        if (celsius > 35) {
            return "⚠️ RECOMMENDATION: Suggest patient reschedule if they have heart/hypertension conditions. " +
                    "Ensure clinic is well air-conditioned.";
        } else if (celsius < 0) {
            return "⚠️ RECOMMENDATION: Warn respiratory patients about cold exposure. " +
                    "Provide parking close to clinic entrance.";
        } else if (celsius >= 30) {
            return "ℹ️ REMINDER: Keep clinic cool. Elderly patients may need assistance.";
        } else {
            return "✓ Weather conditions are suitable for appointments.";
        }
    }

    /**
     * Generate comprehensive health dashboard message
     * Shows current conditions and all relevant health tips
     */
    public static String generateDashboardHealthMessage(double celsius) {
        HealthTip tip = generateHealthTipFromTemperature(celsius);

        return String.format(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "📍 CURRENT WEATHER & HEALTH STATUS\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "%s CURRENT TEMP: %.1f°C\n" +
                        "ALERT LEVEL: %s\n" +
                        "\n%s\n" +
                        "%s\n" +
                        "\n%s\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                tip.icon,
                celsius,
                tip.severity,
                tip.title,
                tip.message,
                getAppointmentRecommendation(celsius));
    }

    /**
     * Check if weather conditions require patient advisories
     */
    public static boolean requiresHealthAdvisory(double celsius) {
        return celsius > 30 || celsius < 5;
    }

    /**
     * Get patients who should be warned (based on temperature)
     */
    public static String[] getAffectedPatientGroups(double celsius) {
        if (celsius > 35) {
            return new String[] { "Cardiac Patients", "Hypertension Patients", "Elderly", "Obese Patients" };
        } else if (celsius < 0) {
            return new String[] { "Respiratory Patients", "Asthma Patients", "Cardiac Patients", "Arthritis Patients" };
        } else if (celsius >= 30) {
            return new String[] { "Elderly", "Diabetic Patients", "Hypertension Patients" };
        } else if (celsius < 5) {
            return new String[] { "Asthma Patients", "COPD Patients", "Joint Pain Patients" };
        }
        return new String[] { "General Population" };
    }

    /**
     * Generate AI prompt for Gemini about weather and health
     * This is used to enhance ChatService context
     */
    public static String generateWeatherHealthContext(double celsius) {
        HealthTip tip = generateHealthTipFromTemperature(celsius);

        return String.format(
                "Current Weather Conditions: %.1f°C\n" +
                        "Alert Level: %s\n" +
                        "Health Tip: %s\n" +
                        "Message: %s\n" +
                        "Affected Groups: %s\n" +
                        "Recommendation: %s",
                celsius,
                tip.severity,
                tip.title,
                tip.message,
                String.join(", ", getAffectedPatientGroups(celsius)),
                getAppointmentRecommendation(celsius));
    }

    /**
     * Color code for health alert (for UI display)
     */
    public static String getAlertColor(double celsius) {
        if (celsius > 35 || celsius < 0) {
            return "#e74c3c"; // Red - Critical
        } else if (celsius >= 30 || (celsius >= 0 && celsius < 5)) {
            return "#f39c12"; // Orange - Warning
        } else {
            return "#27ae60"; // Green - Safe
        }
    }

    /**
     * Get emoji for health status
     */
    public static String getHealthStatusEmoji(double celsius) {
        if (celsius > 35)
            return "🔥";
        if (celsius < 0)
            return "❄️";
        if (celsius >= 30)
            return "☀️";
        if (celsius < 5)
            return "🧊";
        return "✅";
    }
}