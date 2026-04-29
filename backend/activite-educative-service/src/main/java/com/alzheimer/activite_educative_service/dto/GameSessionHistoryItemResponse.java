package com.alzheimer.activite_educative_service.dto;

import com.alzheimer.activite_educative_service.entities.ActivityType;
import com.alzheimer.activite_educative_service.entities.SessionStatus;

import java.time.LocalDateTime;

public class GameSessionHistoryItemResponse {

    private Long sessionId;
    private String patientId;
    private Long activityId;
    private String activityTitle;
    /** Type d’activité ({@link ActivityType}) pour agrégation suivi engagement. */
    private ActivityType activityType;
    private SessionStatus status;
    private Double scorePercent;
    /** Progression 0–100 (questions répondues / total, paires mémoire, etc.). */
    private Integer progressPercentage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public Double getScorePercent() {
        return scorePercent;
    }

    public void setScorePercent(Double scorePercent) {
        this.scorePercent = scorePercent;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
