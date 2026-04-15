package com.alzheimer.event_service.dto;

import java.time.LocalDateTime;

public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String startDateTime;   // ISO string, ex: "2026-03-04T20:33:56"
    private String location;
    private boolean remindEnabled;
    private Long userId;
    private String createdAt;
    private Double lat;
    private Double lng;

    // Constructeur pour initialiser l'objet
    public EventResponse(Long id, String title, String description, LocalDateTime startDateTime, String location, boolean remindEnabled, Long userId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime != null ? startDateTime.toString() : null;
        this.location = location;
        this.remindEnabled = remindEnabled;
        this.userId = userId;
        this.createdAt = createdAt != null ? createdAt.toString() : null;
        // lat/lng not set in this constructor - use the Event entity constructor
    }

    // Constructeur depuis l'entité Event
    public EventResponse(com.alzheimer.event_service.entities.Event e) {
        this.id = e.getId();
        this.title = e.getTitle();
        this.description = e.getDescription();
        this.startDateTime = e.getStartDateTime() != null ? e.getStartDateTime().toString() : null;
        this.location = e.getLocation();
        this.remindEnabled = e.isRemindEnabled();
        this.userId = e.getUserId();
        this.createdAt = e.getCreatedAt() != null ? e.getCreatedAt().toString() : null;
        this.lat = e.getLat();
        this.lng = e.getLng();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isRemindEnabled() {
        return remindEnabled;
    }

    public void setRemindEnabled(boolean remindEnabled) {
        this.remindEnabled = remindEnabled;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
}