package tn.SoftCare.User.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleLoginRequest {

    @NotBlank
    private String credential; // Google ID token (JWT) renvoyé par Google Identity Services

    public String getCredential() { return credential; }
    public void setCredential(String credential) { this.credential = credential; }
}
