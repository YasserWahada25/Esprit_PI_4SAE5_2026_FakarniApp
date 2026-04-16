package com.alzheimer.event_service.dto;

import com.alzheimer.event_service.entities.EventParticipation;
import com.alzheimer.event_service.entities.EventParticipationStatus;

import java.time.LocalDateTime;

public class EventParticipationResponse {

    private Long id;
    private String patientId;
    private Long eventId;
    private String eventTitle;
    private EventParticipationStatus status;
    private int progressPercent;
    private LocalDateTime registeredAt;
    private LocalDateTime updatedAt;

    public EventParticipationResponse() {
    }

    public EventParticipationResponse(EventParticipation e, int progressPercent) {
        this.id = e.getId();
        this.patientId = e.getPatientId();
        this.eventId = e.getEvent().getId();
        this.eventTitle = e.getEvent().getTitle();
        this.status = e.getStatus();
        this.progressPercent = progressPercent;
        this.registeredAt = e.getRegisteredAt();
        this.updatedAt = e.getUpdatedAt();
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

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public EventParticipationStatus getStatus() {
        return status;
    }

    public void setStatus(EventParticipationStatus status) {
        this.status = status;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
