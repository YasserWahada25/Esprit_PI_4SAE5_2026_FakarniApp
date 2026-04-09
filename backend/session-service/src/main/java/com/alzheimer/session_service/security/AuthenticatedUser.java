package com.alzheimer.session_service.security;

import org.springframework.security.oauth2.jwt.Jwt;

public record AuthenticatedUser(String userId, String role) {

    public static AuthenticatedUser fromJwt(Jwt jwt) {
        String userId = jwt.getSubject();
        String role = jwt.getClaimAsString("role");
        return new AuthenticatedUser(
                userId == null ? "" : userId.trim(),
                role == null ? "" : role.trim().toUpperCase()
        );
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
