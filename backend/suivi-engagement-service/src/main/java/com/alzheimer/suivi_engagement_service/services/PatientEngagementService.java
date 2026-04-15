package com.alzheimer.suivi_engagement_service.services;

import com.alzheimer.suivi_engagement_service.dto.EngagementDistributionResponse;
import com.alzheimer.suivi_engagement_service.dto.EngagementRowResponse;
import com.alzheimer.suivi_engagement_service.dto.EngagementSummaryResponse;
import com.alzheimer.suivi_engagement_service.dto.MlDatasetRowResponse;
import com.alzheimer.suivi_engagement_service.entities.EngagementStatus;
import com.alzheimer.suivi_engagement_service.entities.PatientEngagement;
import com.alzheimer.suivi_engagement_service.repositories.PatientEngagementRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientEngagementService {

    private final PatientEngagementRepository repository;

    public PatientEngagementService(PatientEngagementRepository repository) {
        this.repository = repository;
    }

    public List<EngagementRowResponse> getAllEngagements() {
        return repository.findAll().stream()
                .map(EngagementRowResponse::new)
                .collect(Collectors.toList());
    }

    public List<EngagementRowResponse> getEngagementByPatientId(Long patientId) {
        return repository.findByPatientId(patientId).stream()
                .map(EngagementRowResponse::new)
                .collect(Collectors.toList());
    }

    public EngagementSummaryResponse getSummary() {
        List<PatientEngagement> all = repository.findAll();

        long totalActivities = all.stream()
                .map(PatientEngagement::getActivityId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long activePatients = all.stream()
                .map(PatientEngagement::getPatientId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        double avgEngagement = all.stream()
                .filter(e -> e.getProgression() != null)
                .mapToInt(PatientEngagement::getProgression)
                .average()
                .orElse(0.0);

        return new EngagementSummaryResponse(totalActivities, activePatients, avgEngagement);
    }

    public Map<String, Long> getByType() {
        return repository.findAll().stream()
                .filter(e -> e.getActivityType() != null)
                .collect(Collectors.groupingBy(e -> e.getActivityType().name(), Collectors.counting()));
    }

    public EngagementDistributionResponse getDistribution() {
        List<PatientEngagement> all = repository.findAll();

        long completed = all.stream().filter(e -> e.getStatus() == EngagementStatus.COMPLETED).count();
        long inProgress = all.stream().filter(e -> e.getStatus() == EngagementStatus.IN_PROGRESS).count();
        long notStarted = all.stream().filter(e -> e.getStatus() == EngagementStatus.NOT_STARTED).count();
        long abandoned = all.stream().filter(e -> e.getStatus() == EngagementStatus.ABANDONED).count();

        return new EngagementDistributionResponse(completed, inProgress, notStarted, abandoned);
    }

    public List<MlDatasetRowResponse> getMlDataset() {
        List<PatientEngagement> all = repository.findAll();
        Map<Long, List<PatientEngagement>> byPatient = all.stream()
                .filter(e -> e.getPatientId() != null)
                .collect(Collectors.groupingBy(PatientEngagement::getPatientId));

        List<MlDatasetRowResponse> rows = new ArrayList<>();

        for (Map.Entry<Long, List<PatientEngagement>> entry : byPatient.entrySet()) {
            Long patientId = entry.getKey();
            List<PatientEngagement> engagements = entry.getValue();

            String patientName = engagements.stream()
                    .map(PatientEngagement::getPatientName)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);

            double avgScore = engagements.stream()
                    .filter(e -> e.getScore() != null)
                    .mapToInt(PatientEngagement::getScore)
                    .average()
                    .orElse(0.0);

            double avgProgress = engagements.stream()
                    .filter(e -> e.getProgression() != null)
                    .mapToInt(PatientEngagement::getProgression)
                    .average()
                    .orElse(0.0);

            long completed = engagements.stream().filter(e -> e.getStatus() == EngagementStatus.COMPLETED).count();
            long inProgress = engagements.stream().filter(e -> e.getStatus() == EngagementStatus.IN_PROGRESS).count();
            long abandoned = engagements.stream().filter(e -> e.getStatus() == EngagementStatus.ABANDONED).count();

            long total = engagements.size();
            double engagementRate = total == 0 ? 0.0 : (double) completed / (double) total;

            String riskLevel = computeRiskLevel(avgScore, engagementRate, avgProgress, abandoned, total);
            String improvementLabel = computeImprovementLabel(avgScore, engagementRate, avgProgress, abandoned, total);

            rows.add(new MlDatasetRowResponse(
                    patientId,
                    patientName,
                    avgScore,
                    engagementRate,
                    avgProgress,
                    completed,
                    inProgress,
                    abandoned,
                    riskLevel,
                    improvementLabel
            ));
        }

        return rows;
    }

    private String computeRiskLevel(double avgScore, double engagementRate, double avgProgress, long abandoned, long total) {
        double abandonedRate = total == 0 ? 0.0 : (double) abandoned / (double) total;

        if (engagementRate < 0.3 || avgProgress < 30 || abandonedRate > 0.3) {
            return "HIGH";
        }
        if (engagementRate < 0.6 || avgProgress < 60 || avgScore < 60) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String computeImprovementLabel(double avgScore, double engagementRate, double avgProgress, long abandoned, long total) {
        String risk = computeRiskLevel(avgScore, engagementRate, avgProgress, abandoned, total);
        return switch (risk) {
            case "HIGH" -> "NEEDS_ATTENTION";
            case "MEDIUM" -> "IMPROVE";
            default -> "GOOD";
        };
    }
}

