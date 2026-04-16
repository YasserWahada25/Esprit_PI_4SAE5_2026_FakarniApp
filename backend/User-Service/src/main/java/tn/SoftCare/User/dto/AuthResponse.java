package tn.SoftCare.User.dto;

public class AuthResponse {
    private String sessionId;
    private UserResponse user;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }
}