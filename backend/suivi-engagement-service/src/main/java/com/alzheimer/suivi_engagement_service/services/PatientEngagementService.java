package com.alzheimer.suivi_engagement_service.services;

import com.alzheimer.suivi_engagement_service.dto.EngagementDistributionResponse;
import com.alzheimer.suivi_engagement_service.dto.EngagementRowResponse;
import com.alzheimer.suivi_engagement_service.dto.EngagementStatsResponse;
import com.alzheimer.suivi_engagement_service.dto.EngagementSummaryResponse;
import com.alzheimer.suivi_engagement_service.dto.MlDatasetRowResponse;
import com.alzheimer.suivi_engagement_service.entities.EngagementStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientEngagementService {

    private final RemoteEngagementAggregator remoteEngagementAggregator;

    public PatientEngagementService(RemoteEngagementAggregator remoteEngagementAggregator) {
        this.remoteEngagementAggregator = remoteEngagementAggregator;
    }

    public List<EngagementRowResponse> getAllEngagements() {
        return remoteEngagementAggregator.aggregateAll();
    }

    public List<EngagementRowResponse> getEngagementByPatientId(String patientId) {
        return remoteEngagementAggregator.aggregateForPatient(patientId);
    }

    public EngagementSummaryResponse getSummary() {
        return buildSummary(remoteEngagementAggregator.aggregateAll());
    }

    public EngagementStatsResponse getStats() {
        List<EngagementRowResponse> rows = remoteEngagementAggregator.aggregateAll();
        EngagementSummaryResponse base = buildSummary(rows);
        Map<String, Long> byType = rows.stream()
                .filter(e -> e.getActivityType() != null)
                .collect(Collectors.groupingBy(e -> e.getActivityType().name(), Collectors.counting()));
        return new EngagementStatsResponse(
                base.getTotalActivities(),
                base.getActivePatients(),
                base.getAverageEngagement(),
                byType,
                buildDistribution(rows)
        );
    }

    public Map<String, Long> getByType() {
        return remoteEngagementAggregator.aggregateAll().stream()
                .filter(e -> e.getActivityType() != null)
                .collect(Collectors.groupingBy(e -> e.getActivityType().name(), Collectors.counting()));
    }

    public EngagementDistributionResponse getDistribution() {
        return buildDistribution(remoteEngagementAggregator.aggregateAll());
    }

    public List<MlDatasetRowResponse> getMlDataset() {
        List<EngagementRowResponse> all = remoteEngagementAggregator.aggregateAll();
        Map<String, List<EngagementRowResponse>> byPatient = all.stream()
                .filter(e -> e.getPatientId() != null)
                .collect(Collectors.groupingBy(EngagementRowResponse::getPatientId));

        List<MlDatasetRowResponse> rows = new ArrayList<>();

        for (Map.Entry<String, List<EngagementRowResponse>> entry : byPatient.entrySet()) {
            String patientId = entry.getKey();
            List<EngagementRowResponse> engagements = entry.getValue();

            String patientName = engagements.stream()
                    .map(EngagementRowResponse::getPatientName)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);

            double avgScore = engagements.stream()
                    .filter(e -> e.getScore() != null)
                    .mapToInt(EngagementRowResponse::getScore)
                    .average()
                    .orElse(0.0);

            double avgProgress = engagements.stream()
                    .filter(e -> e.getProgression() != null)
                    .mapToInt(EngagementRowResponse::getProgression)
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

    private EngagementSummaryResponse buildSummary(List<EngagementRowResponse> all) {
        long totalActivities = all.stream()
                .map(this::activityOrEventKey)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long activePatients = all.stream()
                .map(EngagementRowResponse::getPatientId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        double avgEngagement = all.stream()
                .filter(e -> e.getProgression() != null)
                .mapToInt(EngagementRowResponse::getProgression)
                .average()
                .orElse(0.0);

        return new EngagementSummaryResponse(totalActivities, activePatients, avgEngagement);
    }

    private EngagementDistributionResponse buildDistribution(List<EngagementRowResponse> all) {
        long completed = all.stream().filter(e -> e.getStatus() == EngagementStatus.COMPLETED).count();
        long inProgress = all.stream().filter(e -> e.getStatus() == EngagementStatus.IN_PROGRESS).count();
        long notStarted = all.stream().filter(e -> e.getStatus() == EngagementStatus.NOT_STARTED).count();
        long abandoned = all.stream().filter(e -> e.getStatus() == EngagementStatus.ABANDONED).count();
        return new EngagementDistributionResponse(completed, inProgress, notStarted, abandoned);
    }

    private String activityOrEventKey(EngagementRowResponse r) {
        if (r.getActivityId() != null) {
            return "A:" + r.getActivityId();
        }
        if (r.getEventId() != null) {
            return "E:" + r.getEventId();
        }
        return null;
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
