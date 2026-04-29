package com.alzheimer.suivi_engagement_service.dto;

public class EngagementDistributionResponse {
    private long completed;
    private long inProgress;
    private long notStarted;
    private long abandoned;

    public EngagementDistributionResponse() {
    }

    public EngagementDistributionResponse(long completed, long inProgress, long notStarted, long abandoned) {
        this.completed = completed;
        this.inProgress = inProgress;
        this.notStarted = notStarted;
        this.abandoned = abandoned;
    }

    public long getCompleted() { return completed; }
    public void setCompleted(long completed) { this.completed = completed; }

    public long getInProgress() { return inProgress; }
    public void setInProgress(long inProgress) { this.inProgress = inProgress; }

    public long getNotStarted() { return notStarted; }
    public void setNotStarted(long notStarted) { this.notStarted = notStarted; }

    public long getAbandoned() { return abandoned; }
    public void setAbandoned(long abandoned) { this.abandoned = abandoned; }
}

