package com.alzheimer.Event_Service.services;

import com.alzheimer.Event_Service.entities.Event;
import com.alzheimer.Event_Service.repositories.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class EventReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventReminderScheduler.class);

    private final EventRepository eventRepository;
    private final EmailService emailService;
    private final UserClient userClient;

    public EventReminderScheduler(EventRepository eventRepository, EmailService emailService, UserClient userClient) {
        this.eventRepository = eventRepository;
        this.emailService = emailService;
        this.userClient = userClient;
    }

    /**
     * S'exécute toutes les minutes pour vérifier les événements qui auront lieu
     * dans les prochaines 24 heures et pour lesquels le rappel (remind_enabled) est activé.
     */
    @Scheduled(fixedRate = 60000)
    public void checkAndSendReminders() {
        log.info("[EventReminderScheduler] Vérification des événements imminents...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24Hours = now.plusDays(1);

        List<Event> eventsToRemind = eventRepository.findByRemindEnabledTrueAndRemindSentFalseAndStartDateTimeBetween(now, next24Hours);

        if (eventsToRemind.isEmpty()) {
            log.info("[EventReminderScheduler] Aucun nouvel événement à rappeler.");
            return;
        }

        for (Event event : eventsToRemind) {
            try {
                // 1. Récupérer le User
                Map<String, Object> userData = userClient.getUserById(event.getUserId());
                String userEmail = (userData != null && userData.containsKey("email")) 
                                   ? userData.get("email").toString() 
                                   : null;

                if (userEmail == null || userEmail.isBlank()) {
                    log.warn("[EventReminderScheduler] Impossible de trouver l'email pour le user_id {}. Rappel ignoré pour l'événement {}.", event.getUserId(), event.getId());
                    continue;
                }

                // 2. Envoyer l'email
                log.info("[EventReminderScheduler] Envoi d'un email de rappel à {} pour l'événement '{}'", userEmail, event.getTitle());
                emailService.sendEventReminder(
                        userEmail, 
                        event.getTitle(), 
                        event.getDescription(), 
                        event.getStartDateTime().toString(), 
                        event.getLocation()
                );

                // 3. Marquer comme envoyé
                event.setRemindSent(true);
                eventRepository.save(event);
                
                log.info("[EventReminderScheduler] Rappel envoyé avec succès pour l'événement '{}'", event.getTitle());

            } catch (Exception e) {
                log.error("[EventReminderScheduler] Erreur lors de l'envoi du rappel pour l'événement '{}': {}", event.getTitle(), e.getMessage());
            }
        }
    }
}
