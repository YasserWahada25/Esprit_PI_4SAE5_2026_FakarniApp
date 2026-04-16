package com.alzheimer.event_service.entities;

/**
 * Chaîne simplifiée participation → suivi engagement (progress indicatif).
 */
public enum EventParticipationStatus {
    /** Inscrit, événement à venir ou non démarré côté patient. */
    REGISTERED,
    /** Participation effective (présence confirmée). */
    ATTENDED,
    TERMINATED,
    CANCELLED
}
