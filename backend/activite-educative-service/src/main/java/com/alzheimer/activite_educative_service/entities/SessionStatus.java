package com.alzheimer.activite_educative_service.entities;

public enum SessionStatus {
    IN_PROGRESS,
    /**
     * Ancien statut « partie terminée » sans distinction réussite / échec.
     * Conservé pour la lecture de lignes déjà en base ; les nouvelles sessions utilisent {@link #SUCCESS} ou {@link #FAILURE}.
     */
    @Deprecated
    COMPLETED,
    /** Partie terminée : pourcentage de bonnes réponses ≥ seuil de l’activité. */
    SUCCESS,
    /** Partie terminée : pourcentage strictement sous le seuil. */
    FAILURE,
    ABANDONED
}
