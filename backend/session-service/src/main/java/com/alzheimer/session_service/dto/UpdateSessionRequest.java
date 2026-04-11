package com.alzheimer.session_service.dto;

import com.alzheimer.session_service.entities.MeetingMode;
import com.alzheimer.session_service.entities.SessionStatus;
import com.alzheimer.session_service.entities.SessionType;
import com.alzheimer.session_service.entities.SessionVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UpdateSessionRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    private String meetingUrl;

    @NotNull
    private SessionStatus status;

    @NotNull
    private SessionVisibility visibility;

    private SessionType sessionType;

    private MeetingMode meetingMode;

    private String locationAddress;

    private Double locationLatitude;

    private Double locationLongitude;

    public @NotBlank String getTitle() {
        return title;
    }

    public void setTitle(@NotBlank String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public @NotNull Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(@NotNull Instant startTime) {
        this.startTime = startTime;
    }

    public @NotNull Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(@NotNull Instant endTime) {
        this.endTime = endTime;
    }

    public String getMeetingUrl() {
        return meetingUrl;
    }

    public void setMeetingUrl(String meetingUrl) {
        this.meetingUrl = meetingUrl;
    }

    public @NotNull SessionStatus getStatus() {
        return status;
    }

    public void setStatus(@NotNull SessionStatus status) {
        this.status = status;
    }

    public @NotNull SessionVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(@NotNull SessionVisibility visibility) {
        this.visibility = visibility;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public MeetingMode getMeetingMode() {
        return meetingMode;
    }

    public void setMeetingMode(MeetingMode meetingMode) {
        this.meetingMode = meetingMode;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public Double getLocationLatitude() {
        return locationLatitude;
    }

    public void setLocationLatitude(Double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public Double getLocationLongitude() {
        return locationLongitude;
    }

    public void setLocationLongitude(Double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }
}
