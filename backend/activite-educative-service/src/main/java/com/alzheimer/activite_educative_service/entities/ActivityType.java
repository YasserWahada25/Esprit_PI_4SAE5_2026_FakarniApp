package com.alzheimer.activite_educative_service.entities;

/**
 * Type d'activité éducative.
 * Valeurs sérialisées en string (JSON) via @Enumerated(EnumType.STRING).
 */
public enum ActivityType {
    /** Contenu pédagogique (lecture, fiches, etc.) — pas de session de jeu obligatoire. */
    CONTENT,
    /** Jeu éducatif — nécessite un {@link GameType} et des questions. */
    GAME,
    /**
     * Ancienne valeur encore présente en base (schéma antérieur).
     * Traitée comme un jeu côté API (scores, sessions) si des questions existent.
     */
    @Deprecated
    QUIZ,
    /**
     * Ancienne valeur encore présente en base.
     * Traitée comme du contenu (équivalent {@link #CONTENT}).
     */
    @Deprecated
    VIDEO
}

