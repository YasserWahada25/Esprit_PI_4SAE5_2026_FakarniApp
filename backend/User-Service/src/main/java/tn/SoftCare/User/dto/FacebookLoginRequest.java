package tn.SoftCare.User.dto;

import jakarta.validation.constraints.NotBlank;

public class FacebookLoginRequest {

    @NotBlank
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
