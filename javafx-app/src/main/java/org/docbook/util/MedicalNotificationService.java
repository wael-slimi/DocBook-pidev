package org.docbook.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class MedicalNotificationService {

    private static final String SENDER_EMAIL = "jebrihassan66@gmail.com";
    private static final String APP_PASSWORD = "jflmnqzhlypbsepw";

    public static void sendEmail(String recipientEmail, String subject, String htmlContent) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email sent successfully to " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Failed to send email to " + recipientEmail);
        }
    }

    public static String getDossierTemplate(String patientName, String dossierNum) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden;'>" +
               "  <div style='background-color: #1e293b; padding: 20px; text-align: center;'>" +
               "    <h1 style='color: white; margin: 0;'>DocBook</h1>" +
               "  </div>" +
               "  <div style='padding: 30px; background-color: white;'>" +
               "    <h2 style='color: #1e293b;'>Nouveau Dossier Médical Créé</h2>" +
               "    <p style='color: #475569;'>Bonjour <strong>" + patientName + "</strong>,</p>" +
               "    <p style='color: #475569;'>Nous vous informons qu'un nouveau dossier médical a été ouvert pour vous dans notre système.</p>" +
               "    <div style='background-color: #f8fafc; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #3b82f6;'>" +
               "      <p style='margin: 0; color: #64748b;'>Numéro de Dossier: <strong style='color: #1e293b;'>" + dossierNum + "</strong></p>" +
               "    </div>" +
               "    <p style='color: #475569;'>Vous pouvez accéder à vos informations en vous connectant à votre espace patient sur DocBook.</p>" +
               "    <hr style='border: 0; border-top: 1px solid #e2e8f0; margin: 30px 0;' />" +
               "    <p style='font-size: 12px; color: #94a3b8; text-align: center;'>Ceci est un message automatique, merci de ne pas y répondre.</p>" +
               "  </div>" +
               "</div>";
    }

    public static String getDocumentTemplate(String patientName, String docTitle, String docType) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden;'>" +
               "  <div style='background-color: #1e293b; padding: 20px; text-align: center;'>" +
               "    <h1 style='color: white; margin: 0;'>DocBook</h1>" +
               "  </div>" +
               "  <div style='padding: 30px; background-color: white;'>" +
               "    <h2 style='color: #1e293b;'>Nouveau Document Disponible</h2>" +
               "    <p style='color: #475569;'>Bonjour <strong>" + patientName + "</strong>,</p>" +
               "    <p style='color: #475569;'>Votre médecin a ajouté un nouveau document à votre dossier médical.</p>" +
               "    <div style='background-color: #f8fafc; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #10b981;'>" +
               "      <p style='margin: 0; color: #64748b;'>Titre: <strong style='color: #1e293b;'>" + docTitle + "</strong></p>" +
               "      <p style='margin: 5px 0 0 0; color: #64748b;'>Type: <strong style='color: #1e293b;'>" + docType + "</strong></p>" +
               "    </div>" +
               "    <p style='color: #475569;'>Connectez-vous à DocBook pour consulter ou télécharger ce document.</p>" +
               "    <hr style='border: 0; border-top: 1px solid #e2e8f0; margin: 30px 0;' />" +
               "    <p style='font-size: 12px; color: #94a3b8; text-align: center;'>L'équipe DocBook vous souhaite une excellente santé.</p>" +
               "  </div>" +
               "</div>";
    }
}