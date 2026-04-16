package com.alzheimer.suivi_engagement_service.services;

import com.alzheimer.suivi_engagement_service.entities.EngagementActivityType;
import com.alzheimer.suivi_engagement_service.entities.EngagementStatus;
import com.alzheimer.suivi_engagement_service.entities.PatientEngagement;
import com.alzheimer.suivi_engagement_service.repositories.PatientEngagementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seed de données mock pour tests métier/IA.
 * Désactivé par défaut : le suivi lit les microservices réels ({@code app.engagement.mock-seed=true} pour activer).
 */
@Component
@ConditionalOnProperty(name = "app.engagement.mock-seed", havingValue = "true")
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
                engagement("mock-patient-1", "Ahmed Ben Ali", 101L, "Quiz sur la Mémoire", EngagementActivityType.QUIZ,
                        EngagementStatus.COMPLETED, 92, 100, now.minusDays(10), now.minusDays(9)),
                engagement("mock-patient-1", "Ahmed Ben Ali", 102L, "Jeu Cognitif - Mémoire Visuelle", EngagementActivityType.GAME,
                        EngagementStatus.IN_PROGRESS, null, 60, now.minusDays(2), null),
                engagement("mock-patient-1", "Ahmed Ben Ali", 103L, "Vidéo - Exercices de Mémoire", EngagementActivityType.VIDEO,
                        EngagementStatus.NOT_STARTED, null, 0, null, null),

                engagement("mock-patient-2", "Fatma Zahra", 201L, "Jeu Cognitif - Attention", EngagementActivityType.GAME,
                        EngagementStatus.ABANDONED, null, 20, now.minusDays(5), now.minusDays(5)),
                engagement("mock-patient-2", "Fatma Zahra", 202L, "Quiz de Culture Générale", EngagementActivityType.QUIZ,
                        EngagementStatus.COMPLETED, 75, 100, now.minusDays(8), now.minusDays(7)),
                engagement("mock-patient-2", "Fatma Zahra", 203L, "Vidéo - Bien-être Cognitif", EngagementActivityType.VIDEO,
                        EngagementStatus.IN_PROGRESS, null, 40, now.minusDays(1), null),

                engagement("mock-patient-3", "Mohamed Salah", 301L, "Quiz - Orientation", EngagementActivityType.QUIZ,
                        EngagementStatus.NOT_STARTED, null, 0, null, null),
                engagement("mock-patient-3", "Mohamed Salah", 302L, "Jeu Cognitif - Sudoku", EngagementActivityType.GAME,
                        EngagementStatus.NOT_STARTED, null, 0, null, null),
                engagement("mock-patient-3", "Mohamed Salah", 303L, "Vidéo - Routine quotidienne", EngagementActivityType.VIDEO,
                        EngagementStatus.ABANDONED, null, 10, now.minusDays(3), now.minusDays(3))
        );

        repository.saveAll(seed);
    }

    private PatientEngagement engagement(
            String patientId,
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

