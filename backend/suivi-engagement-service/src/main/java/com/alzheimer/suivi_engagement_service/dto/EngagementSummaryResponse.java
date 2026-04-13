package com.alzheimer.suivi_engagement_service.dto;

public class EngagementSummaryResponse {
    private long totalActivities;
    private long activePatients;
    private double averageEngagement;

    public EngagementSummaryResponse() {
    }

    public EngagementSummaryResponse(long totalActivities, long activePatients, double averageEngagement) {
        this.totalActivities = totalActivities;
        this.activePatients = activePatients;
        this.averageEngagement = averageEngagement;
    }

    public long getTotalActivities() { return totalActivities; }
    public void setTotalActivities(long totalActivities) { this.totalActivities = totalActivities; }

    public long getActivePatients() { return activePatients; }
    public void setActivePatients(long activePatients) { this.activePatients = activePatients; }

    public double getAverageEngagement() { return averageEngagement; }
    public void setAverageEngagement(double averageEngagement) { this.averageEngagement = averageEngagement; }
}

