package com.alzheimer.Event_Service.services;

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
        message.setSubject("Rappel de l'Ã©vÃ©nement : " + eventTitle);
        
        String body = String.format(
                "Bonjour,\n\nCeci est un rappel automatique pour votre Ã©vÃ©nement Ã  venir :\n\n" +
                "Titre : %s\n" +
                "Date et Heure : %s\n" +
                "Lieu : %s\n\n" +
                "Description :\n%s\n\n" +
                "Ã€ bientÃ´t sur Fakarni !",
                eventTitle, startDateTime, location != null ? location : "Non spÃ©cifiÃ©",
                eventDescription != null ? eventDescription : "Aucune description"
        );
        
        message.setText(body);
        emailSender.send(message);
    }

    public void sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@fakarni.com");
        message.setTo("testuser@example.com");
        message.setSubject("Test Email - VÃ©rification du systÃ¨me d'envoi");
        message.setText("Bonjour Test User,\n\nCeci est un test d'envoi d'email dans le systÃ¨me. " +
                        "Merci de vÃ©rifier si vous avez bien reÃ§u ce message.\n\n" +
                        "L'Ã©quipe Fakarni.");
        
        emailSender.send(message);
    }

    public void sendImmediateReminderToStaticUser(com.alzheimer.Event_Service.entities.Event event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@fakarni.com");
        message.setTo("testuser@example.com");
        message.setSubject("Rappel de votre Ã©vÃ©nement : " + event.getTitle());
        
        String body = String.format(
                "Bonjour Test User,\n\n" +
                "Vous avez un Ã©vÃ©nement prÃ©vu pour bientÃ´t ! Voici les dÃ©tails :\n\n" +
                "- Titre de l'Ã©vÃ©nement : %s\n" +
                "- Description : %s\n" +
                "- Date et Heure : %s\n" +
                "- Lieu : %s\n\n" +
                "Nous vous rappelons que cet Ã©vÃ©nement aura lieu le %s.\n\n" +
                "Ã€ bientÃ´t pour cet Ã©vÃ©nement !",
                event.getTitle(),
                event.getDescription() != null ? event.getDescription() : "Non spÃ©cifiÃ©e",
                event.getStartDateTime(),
                event.getLocation() != null ? event.getLocation() : "Non spÃ©cifiÃ©",
                event.getStartDateTime()
        );
        
        message.setText(body);
        emailSender.send(message);
    }
}

