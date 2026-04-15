package com.alzheimer.suivi_engagement_service.dto;

public class MlDatasetRowResponse {
    private Long patientId;
    private String patientName;
    private double averageScore;
    private double engagementRate;
    private double progression;
    private long completedActivities;
    private long inProgressActivities;
    private long abandonedActivities;
    private String riskLevel;
    private String improvementLabel;

    public MlDatasetRowResponse() {
    }

    public MlDatasetRowResponse(
            Long patientId,
            String patientName,
            double averageScore,
            double engagementRate,
            double progression,
            long completedActivities,
            long inProgressActivities,
            long abandonedActivities,
            String riskLevel,
            String improvementLabel
    ) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.averageScore = averageScore;
        this.engagementRate = engagementRate;
        this.progression = progression;
        this.completedActivities = completedActivities;
        this.inProgressActivities = inProgressActivities;
        this.abandonedActivities = abandonedActivities;
        this.riskLevel = riskLevel;
        this.improvementLabel = improvementLabel;
    }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public double getEngagementRate() { return engagementRate; }
    public void setEngagementRate(double engagementRate) { this.engagementRate = engagementRate; }

    public double getProgression() { return progression; }
    public void setProgression(double progression) { this.progression = progression; }

    public long getCompletedActivities() { return completedActivities; }
    public void setCompletedActivities(long completedActivities) { this.completedActivities = completedActivities; }

    public long getInProgressActivities() { return inProgressActivities; }
    public void setInProgressActivities(long inProgressActivities) { this.inProgressActivities = inProgressActivities; }

    public long getAbandonedActivities() { return abandonedActivities; }
    public void setAbandonedActivities(long abandonedActivities) { this.abandonedActivities = abandonedActivities; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getImprovementLabel() { return improvementLabel; }
    public void setImprovementLabel(String improvementLabel) { this.improvementLabel = improvementLabel; }
}

