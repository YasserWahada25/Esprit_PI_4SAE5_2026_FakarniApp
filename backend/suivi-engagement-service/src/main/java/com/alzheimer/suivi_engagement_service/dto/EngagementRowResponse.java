package com.alzheimer.suivi_engagement_service.dto;

import com.alzheimer.suivi_engagement_service.entities.EngagementActivityType;
import com.alzheimer.suivi_engagement_service.entities.EngagementStatus;
import com.alzheimer.suivi_engagement_service.entities.PatientEngagement;

import java.time.LocalDateTime;

public class EngagementRowResponse {

    private Long id;
    private String patientId;
    private String patientName;
    private Long activityId;
    private Long eventId;
    private String activityTitle;
    private EngagementActivityType activityType;
    private EngagementStatus status;
    private Integer score;
    private Integer progression;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public EngagementRowResponse() {
    }

    public EngagementRowResponse(PatientEngagement e) {
        this.id = e.getId();
        this.patientId = e.getPatientId();
        this.patientName = e.getPatientName();
        this.activityId = e.getActivityId();
        this.eventId = e.getEventId();
        this.activityTitle = e.getActivityTitle();
        this.activityType = e.getActivityType();
        this.status = e.getStatus();
        this.score = e.getScore();
        this.progression = e.getProgression();
        this.startDate = e.getStartDate();
        this.endDate = e.getEndDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public EngagementActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(EngagementActivityType activityType) {
        this.activityType = activityType;
    }

    public EngagementStatus getStatus() {
        return status;
    }

    public void setStatus(EngagementStatus status) {
        this.status = status;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getProgression() {
        return progression;
    }

    public void setProgression(Integer progression) {
        this.progression = progression;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
