package org.docbook.util;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Validation Utility - Handles all form validation logic
 * Provides methods for validating appointments and teleconsultations
 */
public class ValidationUtil {

    // URL validation regex
    private static final String URL_REGEX = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE);

    /**
     * Validate that a field is not empty
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validate that a date is not in the past
     */
    public static boolean isDateNotInPast(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }

    /**
     * Validate that a string is a positive integer within a range
     */
    public static boolean isPositiveInteger(String value) {
        try {
            int num = Integer.parseInt(value.trim());
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate duration is within acceptable range (5-480 minutes = 5min to 8 hours)
     */
    public static boolean isValidDuration(String value) {
        try {
            int duration = Integer.parseInt(value.trim());
            return duration >= 5 && duration <= 480;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate URL format
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url.trim()).matches();
    }

    /**
     * Validate time format (HH:mm)
     */
    public static boolean isValidTimeFormat(String time) {
        try {
            java.time.LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Apply error styling to a TextField
     */
    public static void setFieldError(TextField field, boolean isError) {
        if (isError) {
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 4;");
        } else {
            field.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 6;");
        }
    }

    /**
     * Apply error styling to a DatePicker
     */
    public static void setDatePickerError(DatePicker picker, boolean isError) {
        if (isError) {
            picker.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 4;");
        } else {
            picker.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 6;");
        }
    }

    /**
     * Apply error styling to a TextArea
     */
    public static void setTextAreaError(TextArea area, boolean isError) {
        if (isError) {
            area.setStyle(
                    "-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 4; -fx-control-inner-background: #ffe6e6;");
        } else {
            area.setStyle(
                    "-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 6; -fx-control-inner-background: #ffffff;");
        }
    }

    /**
     * Clear all error styling from a TextField
     */
    public static void clearFieldError(TextField field) {
        field.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 6;");
    }

    /**
     * Clear all error styling from a DatePicker
     */
    public static void clearDatePickerError(DatePicker picker) {
        picker.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 6;");
    }

    /**
     * Clear all error styling from a TextArea
     */
    public static void clearTextAreaError(TextArea area) {
        area.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 6;");
    }
}
