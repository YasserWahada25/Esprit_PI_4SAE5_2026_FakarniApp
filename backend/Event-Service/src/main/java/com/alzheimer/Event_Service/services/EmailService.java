package com.alzheimer.event_service.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendEventReminder(String to, String eventTitle, String eventDescription, String startDateTime, String location) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@fakarni.com");
        message.setTo(to);
        message.setSubject("Rappel de l'événement : " + eventTitle);
        
        String body = String.format(
                "Bonjour,\n\nCeci est un rappel automatique pour votre événement à venir :\n\n" +
                "Titre : %s\n" +
                "Date et Heure : %s\n" +
                "Lieu : %s\n\n" +
                "Description :\n%s\n\n" +
                "À bientôt sur Fakarni !",
                eventTitle, startDateTime, location != null ? location : "Non spécifié",
                eventDescription != null ? eventDescription : "Aucune description"
        );
        
        message.setText(body);
        emailSender.send(message);
    }

    public void sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@fakarni.com");
        message.setTo("testuser@example.com");
        message.setSubject("Test Email - Vérification du système d'envoi");
        message.setText("Bonjour Test User,\n\nCeci est un test d'envoi d'email dans le système. " +
                        "Merci de vérifier si vous avez bien reçu ce message.\n\n" +
                        "L'équipe Fakarni.");
        
        emailSender.send(message);
    }

    public void sendImmediateReminderToStaticUser(com.alzheimer.event_service.entities.Event event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@fakarni.com");
        message.setTo("testuser@example.com");
        message.setSubject("Rappel de votre événement : " + event.getTitle());
        
        String body = String.format(
                "Bonjour Test User,\n\n" +
                "Vous avez un événement prévu pour bientôt ! Voici les détails :\n\n" +
                "- Titre de l'événement : %s\n" +
                "- Description : %s\n" +
                "- Date et Heure : %s\n" +
                "- Lieu : %s\n\n" +
                "Nous vous rappelons que cet événement aura lieu le %s.\n\n" +
                "À bientôt pour cet événement !",
                event.getTitle(),
                event.getDescription() != null ? event.getDescription() : "Non spécifiée",
                event.getStartDateTime(),
                event.getLocation() != null ? event.getLocation() : "Non spécifié",
                event.getStartDateTime()
        );
        
        message.setText(body);
        emailSender.send(message);
    }
}
