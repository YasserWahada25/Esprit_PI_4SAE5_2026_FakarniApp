package com.alzheimer.suivi_engagement_service.dto;

import java.util.Map;

/**
 * Statistiques agrégées pour {@code GET /api/engagement/stats}.
 */
public class EngagementStatsResponse {

    private long totalActivities;
    private long activePatients;
    private double averageEngagement;
    private Map<String, Long> participationByType;
    private EngagementDistributionResponse distributionByStatus;

    public EngagementStatsResponse() {
    }

    public EngagementStatsResponse(
            long totalActivities,
            long activePatients,
            double averageEngagement,
            Map<String, Long> participationByType,
            EngagementDistributionResponse distributionByStatus
    ) {
        this.totalActivities = totalActivities;
        this.activePatients = activePatients;
        this.averageEngagement = averageEngagement;
        this.participationByType = participationByType;
        this.distributionByStatus = distributionByStatus;
    }

    public long getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(long totalActivities) {
        this.totalActivities = totalActivities;
    }

    public long getActivePatients() {
        return activePatients;
    }

    public void setActivePatients(long activePatients) {
        this.activePatients = activePatients;
    }

    public double getAverageEngagement() {
        return averageEngagement;
    }

    public void setAverageEngagement(double averageEngagement) {
        this.averageEngagement = averageEngagement;
    }

    public Map<String, Long> getParticipationByType() {
        return participationByType;
    }

    public void setParticipationByType(Map<String, Long> participationByType) {
        this.participationByType = participationByType;
    }

    public EngagementDistributionResponse getDistributionByStatus() {
        return distributionByStatus;
    }

    public void setDistributionByStatus(EngagementDistributionResponse distributionByStatus) {
        this.distributionByStatus = distributionByStatus;
    }
}
