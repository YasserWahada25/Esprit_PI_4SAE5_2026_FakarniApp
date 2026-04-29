package com.alzheimer.activite_educative_service.dto;

import com.alzheimer.activite_educative_service.entities.ActivityStatus;
import com.alzheimer.activite_educative_service.entities.ActivityType;
import com.alzheimer.activite_educative_service.entities.GameType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActiviteEducativeRequest {

    @NotBlank
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull
    private ActivityType type;

    private ActivityStatus status;

    @JsonAlias({"game_type", "GAME_TYPE"})
    private GameType gameType;

    @Size(max = 64)
    @JsonAlias({"icon_key", "ICON_KEY"})
    private String iconKey;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @JsonAlias({"score_threshold", "SCORE_THRESHOLD"})
    private Double scoreThreshold;

    @Size(max = 2000)
    @JsonAlias({"thumbnail_url", "THUMBNAIL_URL"})
    private String thumbnailUrl;

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
}

