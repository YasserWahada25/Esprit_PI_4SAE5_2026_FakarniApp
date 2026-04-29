package com.alzheimer.event_service.dto;

import jakarta.validation.constraints.NotBlank;

public class EventParticipationRequest {

    @NotBlank
    private String patientId;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
