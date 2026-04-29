package com.alzheimer.activite_educative_service.dto;

import com.alzheimer.activite_educative_service.entities.ActiviteEducative;
import com.alzheimer.activite_educative_service.entities.ActivityStatus;
import com.alzheimer.activite_educative_service.entities.ActivityType;
import com.alzheimer.activite_educative_service.entities.GameType;

import java.time.LocalDateTime;

public class ActiviteEducativeResponse {

    private Long id;
    private String title;
    private String description;
    private ActivityType type;
    private GameType gameType;
    private String iconKey;
    private LocalDateTime createdAt;
    private ActivityStatus status;
    private Double scoreThreshold;
    /** Dernier score connu pour l’utilisateur (sessions terminées), si demandé via ?userId=. */
    private Double latestScorePercent;
    private String thumbnailUrl;
    private java.time.LocalDateTime updatedAt;

    public ActiviteEducativeResponse() {
    }

    public ActiviteEducativeResponse(ActiviteEducative entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.type = entity.getType();
        this.gameType = entity.getGameType();
        this.iconKey = entity.getIconKey();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
        this.thumbnailUrl = entity.getThumbnailUrl();
        this.status = entity.getStatus();
        this.scoreThreshold = entity.getScoreThreshold();
    }

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

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public void setStatus(ActivityStatus status) {
        this.status = status;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    public Double getLatestScorePercent() {
        return latestScorePercent;
    }

    public void setLatestScorePercent(Double latestScorePercent) {
        this.latestScorePercent = latestScorePercent;
    }

    public Double getScoreThreshold() {
        return scoreThreshold;
    }

    public void setScoreThreshold(Double scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

