package com.alzheimer.suivi_engagement_service.services;

import com.alzheimer.suivi_engagement_service.entities.EngagementActivityType;
import com.alzheimer.suivi_engagement_service.entities.EngagementStatus;
import com.alzheimer.suivi_engagement_service.entities.PatientEngagement;
import com.alzheimer.suivi_engagement_service.repositories.PatientEngagementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seed de données mock pour tests métier/IA.
 * Se lance au démarrage standard Spring Boot, uniquement si la table est vide.
 */
@Component
public class MockPatientEngagementDataLoader implements CommandLineRunner {

    private final PatientEngagementRepository repository;

    public MockPatientEngagementDataLoader(PatientEngagementRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        List<PatientEngagement> seed = List.of(
                // Patient 1: score élevé sur QUIZ terminé + GAME en cours + VIDEO pas commencé
                engagement(1L, "Ahmed Ben Ali", 101L, "Quiz sur la Mémoire", EngagementActivityType.QUIZ,
                        EngagementStatus.COMPLETED, 92, 100, now.minusDays(10), now.minusDays(9)),
                engagement(1L, "Ahmed Ben Ali", 102L, "Jeu Cognitif - Mémoire Visuelle", EngagementActivityType.GAME,
                        EngagementStatus.IN_PROGRESS, null, 60, now.minusDays(2), null),
                engagement(1L, "Ahmed Ben Ali", 103L, "Vidéo - Exercices de Mémoire", EngagementActivityType.VIDEO,
                        EngagementStatus.NOT_STARTED, null, 0, null, null),

                // Patient 2: activité abandonnée + activité terminée moyenne + activité en cours
                engagement(2L, "Fatma Zahra", 201L, "Jeu Cognitif - Attention", EngagementActivityType.GAME,
                        EngagementStatus.ABANDONED, null, 20, now.minusDays(5), now.minusDays(5)),
                engagement(2L, "Fatma Zahra", 202L, "Quiz de Culture Générale", EngagementActivityType.QUIZ,
                        EngagementStatus.COMPLETED, 75, 100, now.minusDays(8), now.minusDays(7)),
                engagement(2L, "Fatma Zahra", 203L, "Vidéo - Bien-être Cognitif", EngagementActivityType.VIDEO,
                        EngagementStatus.IN_PROGRESS, null, 40, now.minusDays(1), null),

                // Patient 3: faible engagement global
                engagement(3L, "Mohamed Salah", 301L, "Quiz - Orientation", EngagementActivityType.QUIZ,
                        EngagementStatus.NOT_STARTED, null, 0, null, null),
                engagement(3L, "Mohamed Salah", 302L, "Jeu Cognitif - Sudoku", EngagementActivityType.GAME,
                        EngagementStatus.NOT_STARTED, null, 0, null, null),
                engagement(3L, "Mohamed Salah", 303L, "Vidéo - Routine quotidienne", EngagementActivityType.VIDEO,
                        EngagementStatus.ABANDONED, null, 10, now.minusDays(3), now.minusDays(3))
        );

        repository.saveAll(seed);
    }

    private PatientEngagement engagement(
            Long patientId,
            String patientName,
            Long activityId,
            String activityTitle,
            EngagementActivityType type,
            EngagementStatus status,
            Integer score,
            Integer progression,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        PatientEngagement e = new PatientEngagement();
        e.setPatientId(patientId);
        e.setPatientName(patientName);
        e.setActivityId(activityId);
        e.setActivityTitle(activityTitle);
        e.setActivityType(type);
        e.setStatus(status);
        e.setScore(score);
        e.setProgression(progression);
        e.setStartDate(startDate);
        e.setEndDate(endDate);
        return e;
    }
}

