package tn.SoftCare.User.security;

import tn.SoftCare.User.model.Role;

public class AuthenticatedUser {
    private final String sessionId;
    private final String id;
    private final String email;
    private final Role role;

    public AuthenticatedUser(String sessionId, String id, String email, Role role) {
        this.sessionId = sessionId;
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}
