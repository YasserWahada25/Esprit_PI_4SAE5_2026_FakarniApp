package com.alzheimer.Event_Service.entities;

/**
 * ChaÃ®ne simplifiÃ©e participation â†’ suivi engagement (progress indicatif).
 */
public enum EventParticipationStatus {
    /** Inscrit, Ã©vÃ©nement Ã  venir ou non dÃ©marrÃ© cÃ´tÃ© patient. */
    REGISTERED,
    /** Participation effective (prÃ©sence confirmÃ©e). */
    ATTENDED,
    TERMINATED,
    CANCELLED
}

