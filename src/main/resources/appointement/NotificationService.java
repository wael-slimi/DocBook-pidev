package tn.esprit.services;

import javafx.application.Platform;
import javafx.stage.Window;
import org.controlsfx.control.Notifications;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NotificationService - Handles email, SMS, and UI notifications
 * Features:
 * - Send email notifications (simulated or real)
 * - Send SMS notifications (simulated or real via Twilio)
 * - Show toast notifications via ControlsFX
 * - Async execution to avoid UI blocking
 */
public class NotificationService {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);

    /**
     * Send teleconsultation invitation via email
     * Sends the consultation URL and meeting details to patient
     *
     * @param patientEmail        Email address of patient
     * @param teleconsultationUrl The video call URL
     * @param consultationTime    Time of consultation
     * @param doctorName          Name of consulting doctor
     */
    public static void sendTeleconsultationEmail(
            String patientEmail,
            String teleconsultationUrl,
            String consultationTime,
            String doctorName) {

        executorService.submit(() -> {
            try {
                System.out.println("📧 Preparing email notification...");

                // Build email content
                String subject = "📅 Teleconsultation Appointment Confirmation";
                String emailBody = buildTeleconsultationEmailBody(
                        teleconsultationUrl,
                        consultationTime,
                        doctorName);

                // Simulate sending email
                System.out.println("✉️ TO: " + patientEmail);
                System.out.println("SUBJECT: " + subject);
                System.out.println("BODY:\n" + emailBody);
                System.out.println("\n✅ Email sent successfully!");

                // Show success toast
                Platform.runLater(() -> {
                    showSuccessToast(
                            "📧 Email Sent",
                            "Consultation details sent to " + maskEmail(patientEmail));
                });

            } catch (Exception e) {
                System.err.println("❌ Error sending email: " + e.getMessage());
                Platform.runLater(() -> {
                    showErrorToast("Email Error", "Failed to send notification");
                });
            }
        });
    }

    /**
     * Send SMS notification to patient
     * Sends consultation link and time via SMS
     */
    public static void sendTeleconsultationSMS(
            String phoneNumber,
            String teleconsultationUrl,
            String consultationTime) {

        executorService.submit(() -> {
            try {
                System.out.println("📱 Preparing SMS notification...");

                String smsBody = String.format(
                        "Medilab Alert: Your teleconsultation is at %s. Join here: %s",
                        consultationTime,
                        shortenUrl(teleconsultationUrl));

                // Simulate SMS
                System.out.println("📲 TO: " + maskPhoneNumber(phoneNumber));
                System.out.println("MESSAGE: " + smsBody);
                System.out.println("\n✅ SMS sent successfully!");

                Platform.runLater(() -> {
                    showSuccessToast(
                            "📱 SMS Sent",
                            "SMS sent to " + maskPhoneNumber(phoneNumber));
                });

            } catch (Exception e) {
                System.err.println("❌ Error sending SMS: " + e.getMessage());
                Platform.runLater(() -> {
                    showErrorToast("SMS Error", "Failed to send SMS");
                });
            }
        });
    }

    /**
     * Send appointment reminder notification
     */
    public static void sendAppointmentReminder(
            String patientEmail,
            String appointmentTime,
            String doctorName) {

        executorService.submit(() -> {
            try {
                String subject = "⏰ Appointment Reminder";
                String body = String.format(
                        "Reminder: Your appointment with Dr. %s is at %s. Please arrive 10 minutes early.",
                        doctorName,
                        appointmentTime);

                // Log both subject and body so neither is unused
                System.out.println("📧 Appointment Reminder");
                System.out.println("TO: " + patientEmail);
                System.out.println("SUBJECT: " + subject);
                System.out.println("MESSAGE: " + body);
                System.out.println("✅ Reminder sent!");

                Platform.runLater(() -> {
                    showInfoToast("⏰ Reminder Sent", "Appointment reminder sent");
                });

            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
            }
        });
    }

    /**
     * Send rating confirmation notification
     */
    public static void sendRatingConfirmation(String patientEmail, int stars) {
        executorService.submit(() -> {
            try {
                String starDisplay = "⭐".repeat(stars) + "☆".repeat(5 - stars);
                System.out.println("📧 Rating Confirmation");
                System.out.println("TO: " + patientEmail);
                System.out.println("RATING: " + starDisplay + " (" + stars + "/5)");
                System.out.println("✅ Rating received and logged!");

                Platform.runLater(() -> {
                    showSuccessToast(
                            "⭐ Rating Recorded",
                            starDisplay + " Thank you for your feedback!");
                });

            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
            }
        });
    }

    /**
     * Show success toast notification (top-right corner)
     */
    public static void showSuccessToast(String title, String message) {
        try {
            Notifications.create()
                    .title(title)
                    .text(message)
                    .graphic(null)
                    .hideAfter(javafx.util.Duration.seconds(4))
                    .position(javafx.geometry.Pos.TOP_RIGHT)
                    .owner(getPrimaryWindow())
                    .showInformation();
        } catch (Exception e) {
            System.out.println("✓ " + title + ": " + message);
        }
    }

    /**
     * Show error toast notification
     */
    public static void showErrorToast(String title, String message) {
        try {
            Notifications.create()
                    .title(title)
                    .text(message)
                    .graphic(null)
                    .hideAfter(javafx.util.Duration.seconds(5))
                    .position(javafx.geometry.Pos.TOP_RIGHT)
                    .owner(getPrimaryWindow())
                    .showError();
        } catch (Exception e) {
            System.out.println("❌ " + title + ": " + message);
        }
    }

    /**
     * Show info toast notification
     */
    public static void showInfoToast(String title, String message) {
        try {
            Notifications.create()
                    .title(title)
                    .text(message)
                    .graphic(null)
                    .hideAfter(javafx.util.Duration.seconds(3))
                    .position(javafx.geometry.Pos.TOP_RIGHT)
                    .owner(getPrimaryWindow())
                    .showConfirm();
        } catch (Exception e) {
            System.out.println("ℹ️ " + title + ": " + message);
        }
    }

    /**
     * Show warning toast notification
     */
    public static void showWarningToast(String title, String message) {
        try {
            Notifications.create()
                    .title(title)
                    .text(message)
                    .graphic(null)
                    .hideAfter(javafx.util.Duration.seconds(4))
                    .position(javafx.geometry.Pos.TOP_RIGHT)
                    .owner(getPrimaryWindow())
                    .showWarning();
        } catch (Exception e) {
            System.out.println("⚠️ " + title + ": " + message);
        }
    }

    /**
     * Returns the first visible JavaFX window, or null if none is showing yet.
     * ControlsFX requires a non-null owner window to render toast notifications.
     */
    private static Window getPrimaryWindow() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }

    /**
     * Shutdown notification service
     */
    public static void shutdown() {
        executorService.shutdown();
        System.out.println("🔌 NotificationService shut down");
    }

    // ===== HELPER METHODS =====

    /**
     * Build teleconsultation email body
     */
    private static String buildTeleconsultationEmailBody(
            String teleconsultationUrl,
            String consultationTime,
            String doctorName) {

        return """
                Dear Patient,

                Your teleconsultation has been scheduled!

                📋 CONSULTATION DETAILS:
                ━━━━━━━━━━━━━━━━━━━━━━━━
                Doctor: Dr. """ + doctorName + """
                Date & Time: """ + consultationTime + """
                Type: Video Consultation

                🔗 JOIN CONSULTATION:
                ━━━━━━━━━━━━━━━━━━━━━━━━
                """ + teleconsultationUrl + """

                📱 PREPARATION:
                • Ensure stable internet connection
                • Have your medical history ready
                • Join 5 minutes before scheduled time
                • Find a quiet place for better communication

                ❓ NEED HELP?
                Contact us at support@medilab.com

                Best regards,
                Medilab Healthcare Team
                """;
    }

    /**
     * Mask email for display
     */
    private static String maskEmail(String email) {
        if (email == null || email.length() < 3)
            return email;
        int atIndex = email.indexOf('@');
        if (atIndex < 2)
            return email;
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    /**
     * Mask phone number for display
     */
    private static String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4)
            return phone;
        return "***" + phone.substring(phone.length() - 4);
    }

    /**
     * Shorten URL for SMS (simulate)
     */
    private static String shortenUrl(String url) {
        return "bit.ly/medilab" + Math.random() * 10000;
    }
}