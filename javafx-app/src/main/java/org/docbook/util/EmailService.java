package org.docbook.util;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

    private static final String SENDER_EMAIL = EnvLoader.get("MAIL_SENDER_EMAIL");
    private static final String SENDER_PASSWORD = EnvLoader.get("MAIL_SENDER_PASSWORD");

    public static boolean sendEmail(String recipientEmail, String subject, String body) {
        if (SENDER_EMAIL == null || SENDER_PASSWORD == null) {
            System.err.println("Email configuration error: MAIL_SENDER_EMAIL or MAIL_SENDER_PASSWORD not set in .env");
            return false;
        }

        System.out.println("--- SENDING EMAIL ---");
        System.out.println("To: " + recipientEmail);
        System.out.println("From: " + SENDER_EMAIL);
        System.out.println("Subject: " + subject);

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully to " + recipientEmail);
            return true;
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
