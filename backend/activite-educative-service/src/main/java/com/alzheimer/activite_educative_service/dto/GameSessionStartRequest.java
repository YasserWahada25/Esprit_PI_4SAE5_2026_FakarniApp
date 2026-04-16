package com.alzheimer.activite_educative_service.dto;

/**
 * Démarrage de session : préférer {@code patientId} (identifiant User-Service / MongoDB).
 * {@code userId} numérique reste accepté pour compatibilité d’anciens clients/tests.
 */
public class GameSessionStartRequest {

    private String patientId;

    @Deprecated
    private Long userId;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    @Deprecated
    public Long getUserId() {
        return userId;
    }

    @Deprecated
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
