package com.alzheimer.suivi_engagement_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Miroir de {@code tn.SoftCare.User.dto.PatientSummaryDto} (User-Service /internal/users/patients).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientSummaryFeignDto {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String displayName() {
        String fn = firstName != null ? firstName.trim() : "";
        String ln = lastName != null ? lastName.trim() : "";
        String both = (fn + " " + ln).trim();
        return both.isEmpty() ? (id != null ? id : "Patient") : both;
    }
}
