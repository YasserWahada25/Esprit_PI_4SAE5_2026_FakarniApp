package tn.SoftCare.User.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleLoginRequest {

    @NotBlank(message = "Google ID token is required")
    private String credential; // Google ID token (JWT) from Google Identity Services

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
}
