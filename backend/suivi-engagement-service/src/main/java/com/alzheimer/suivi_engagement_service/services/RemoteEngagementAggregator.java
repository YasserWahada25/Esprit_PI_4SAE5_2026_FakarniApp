package com.alzheimer.suivi_engagement_service.services;

import com.alzheimer.suivi_engagement_service.client.*;
import com.alzheimer.suivi_engagement_service.dto.EngagementRowResponse;
import com.alzheimer.suivi_engagement_service.entities.EngagementActivityType;
import com.alzheimer.suivi_engagement_service.entities.EngagementStatus;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Agrège les données réelles User + Activité + Événements (priorité rôle patient côté User-Service).
 */
@Component
public class RemoteEngagementAggregator {

    private static final Logger log = LoggerFactory.getLogger(RemoteEngagementAggregator.class);

    private final UserPatientsFeignClient userPatientsFeignClient;
    private final ActivitySessionsFeignClient activitySessionsFeignClient;
    private final EventParticipationsFeignClient eventParticipationsFeignClient;

    public RemoteEngagementAggregator(
            UserPatientsFeignClient userPatientsFeignClient,
            ActivitySessionsFeignClient activitySessionsFeignClient,
            EventParticipationsFeignClient eventParticipationsFeignClient
    ) {
        this.userPatientsFeignClient = userPatientsFeignClient;
        this.activitySessionsFeignClient = activitySessionsFeignClient;
        this.eventParticipationsFeignClient = eventParticipationsFeignClient;
    }

    public List<EngagementRowResponse> aggregateAll() {
        List<PatientSummaryFeignDto> patients = safeListPatients();
        Map<String, String> names = patients.stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(PatientSummaryFeignDto::getId, PatientSummaryFeignDto::displayName, (a, b) -> a));
        Set<String> patientIds = new HashSet<>(names.keySet());

        List<EngagementRowResponse> rows = new ArrayList<>(safeEngagementSessionsBulk(names, patientIds));

        for (EngagementRowResponse r : rows) {
            if (r.getPatientId() != null) {
                patientIds.add(r.getPatientId());
            }
        }

        rows.addAll(safeEventRows(names, patientIds));
        rows.sort(Comparator.comparing(EngagementRowResponse::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return rows;
    }

    public List<EngagementRowResponse> aggregateForPatient(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            return List.of();
        }
        String pid = patientId.trim();
        Map<String, String> names = new HashMap<>();
        try {
            userPatientsFeignClient.listPatients().stream()
                    .filter(p -> pid.equals(p.getId()))
                    .findFirst()
                    .ifPresent(p -> names.put(pid, p.displayName()));
        } catch (FeignException e) {
            log.warn("[Suivi] User-Service indisponible pour résolution nom patient {}: {}", pid, e.getMessage());
        }
        if (!names.containsKey(pid)) {
            names.put(pid, pid);
        }
        List<EngagementRowResponse> rows = new ArrayList<>(safeHistory(pid, names));
        rows.addAll(safeEventRowsForPatient(pid, names));
        rows.sort(Comparator.comparing(EngagementRowResponse::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return rows;
    }

    private List<PatientSummaryFeignDto> safeListPatients() {
        try {
            List<PatientSummaryFeignDto> list = userPatientsFeignClient.listPatients();
            return list != null ? list : List.of();
        } catch (FeignException e) {
            log.warn("[Suivi] Impossible de charger les patients (User-Service): {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Charge les sessions via un seul appel activité. Si la liste User n’est pas vide, on ne garde
     * que les lignes des patients connus (rôle PATIENT côté User-Service) ; sinon on affiche tout ce
     * que renvoie l’activité (évite tableau vide quand Feign User / Eureka est HS).
     */
    private List<EngagementRowResponse> safeEngagementSessionsBulk(Map<String, String> names, Set<String> knownPatientIds) {
        try {
            List<GameSessionHistoryFeignDto> list = activitySessionsFeignClient.engagementSessions(500);
            if (list == null) {
                return List.of();
            }
            boolean restrictToKnownPatients = !knownPatientIds.isEmpty();
            return list.stream()
                    .filter(s -> s.getPatientId() != null && !s.getPatientId().isBlank())
                    .filter(s -> !restrictToKnownPatients || knownPatientIds.contains(s.getPatientId()))
                    .map(s -> {
                        String pid = s.getPatientId();
                        String label = names.getOrDefault(pid, fallbackPatientLabel(pid));
                        return fromGameSession(s, label);
                    })
                    .collect(Collectors.toList());
        } catch (FeignException e) {
            log.warn("[Suivi] engagement-sessions (activité) indisponible: {}", e.getMessage());
            return List.of();
        }
    }

    private static String fallbackPatientLabel(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            return "Patient";
        }
        String t = patientId.trim();
        return t.length() <= 12 ? t : (t.substring(0, 8) + "…");
    }

    private List<EngagementRowResponse> safeHistory(String patientId, Map<String, String> names) {
        try {
            List<GameSessionHistoryFeignDto> list = activitySessionsFeignClient.history(patientId);
            if (list == null) {
                return List.of();
            }
            String name = names.getOrDefault(patientId, patientId);
            return list.stream().map(s -> fromGameSession(s, name)).collect(Collectors.toList());
        } catch (FeignException e) {
            log.warn("[Suivi] Historique activités indisponible pour patient {}: {}", patientId, e.getMessage());
            return List.of();
        }
    }

    private List<EngagementRowResponse> safeEventRows(Map<String, String> patientNames, Set<String> allowedPatientIds) {
        try {
            List<EventParticipationFeignDto> list = eventParticipationsFeignClient.listParticipations(null);
            if (list == null) {
                return List.of();
            }
            boolean restrict = !allowedPatientIds.isEmpty();
            return list.stream()
                    .filter(p -> p.getPatientId() != null && !p.getPatientId().isBlank())
                    .filter(p -> !restrict || allowedPatientIds.contains(p.getPatientId()))
                    .map(p -> fromEventParticipation(
                            p,
                            patientNames.getOrDefault(p.getPatientId(), fallbackPatientLabel(p.getPatientId()))
                    ))
                    .collect(Collectors.toList());
        } catch (FeignException e) {
            log.warn("[Suivi] Participations événements indisponibles: {}", e.getMessage());
            return List.of();
        }
    }

    private List<EngagementRowResponse> safeEventRowsForPatient(String patientId, Map<String, String> names) {
        try {
            List<EventParticipationFeignDto> list = eventParticipationsFeignClient.listParticipations(patientId);
            if (list == null) {
                return List.of();
            }
            String name = names.getOrDefault(patientId, patientId);
            return list.stream().map(p -> fromEventParticipation(p, name)).collect(Collectors.toList());
        } catch (FeignException e) {
            log.warn("[Suivi] Participations événements indisponibles pour {}: {}", patientId, e.getMessage());
            return List.of();
        }
    }

    private EngagementRowResponse fromGameSession(GameSessionHistoryFeignDto s, String patientName) {
        EngagementRowResponse r = new EngagementRowResponse();
        r.setId(s.getSessionId());
        r.setPatientId(s.getPatientId());
        r.setPatientName(patientName);
        r.setActivityId(s.getActivityId());
        r.setActivityTitle(s.getActivityTitle());
        r.setEventId(null);
        r.setActivityType(mapActivityType(s.getActivityType()));
        r.setStatus(mapSessionStatus(s.getStatus()));
        r.setScore(scoreInt(s.getScorePercent()));
        r.setProgression(s.getProgressPercentage() != null ? s.getProgressPercentage() : 0);
        r.setStartDate(s.getStartedAt());
        r.setEndDate(s.getFinishedAt());
        return r;
    }

    private EngagementRowResponse fromEventParticipation(EventParticipationFeignDto p, String patientName) {
        EngagementRowResponse r = new EngagementRowResponse();
        r.setId(p.getId());
        r.setPatientId(p.getPatientId());
        r.setPatientName(patientName);
        r.setActivityId(null);
        r.setEventId(p.getEventId());
        r.setActivityTitle(p.getEventTitle());
        r.setActivityType(EngagementActivityType.EVENT);
        r.setStatus(mapEventParticipationStatus(p.getStatus()));
        r.setScore(null);
        r.setProgression(p.getProgressPercent());
        r.setStartDate(p.getRegisteredAt());
        r.setEndDate(p.getUpdatedAt());
        return r;
    }

    private static EngagementActivityType mapActivityType(String raw) {
        if (raw == null) {
            return EngagementActivityType.GAME;
        }
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "QUIZ" -> EngagementActivityType.QUIZ;
            case "VIDEO" -> EngagementActivityType.VIDEO;
            case "GAME" -> EngagementActivityType.GAME;
            case "CONTENT" -> EngagementActivityType.VIDEO;
            default -> EngagementActivityType.GAME;
        };
    }

    private static EngagementStatus mapSessionStatus(String raw) {
        if (raw == null) {
            return EngagementStatus.NOT_STARTED;
        }
        return switch (raw.trim()) {
            case "SUCCESS", "FAILURE", "COMPLETED" -> EngagementStatus.COMPLETED;
            case "IN_PROGRESS" -> EngagementStatus.IN_PROGRESS;
            case "ABANDONED" -> EngagementStatus.ABANDONED;
            default -> EngagementStatus.IN_PROGRESS;
        };
    }

    private static EngagementStatus mapEventParticipationStatus(String raw) {
        if (raw == null) {
            return EngagementStatus.NOT_STARTED;
        }
        return switch (raw.trim()) {
            case "REGISTERED" -> EngagementStatus.NOT_STARTED;
            case "ATTENDED" -> EngagementStatus.IN_PROGRESS;
            case "TERMINATED" -> EngagementStatus.COMPLETED;
            case "CANCELLED" -> EngagementStatus.ABANDONED;
            default -> EngagementStatus.NOT_STARTED;
        };
    }

    private static Integer scoreInt(Double scorePercent) {
        if (scorePercent == null) {
            return null;
        }
        return (int) Math.round(scorePercent);
    }
}
