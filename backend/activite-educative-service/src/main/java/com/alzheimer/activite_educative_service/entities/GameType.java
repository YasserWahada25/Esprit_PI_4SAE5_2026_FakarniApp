package com.alzheimer.activite_educative_service.entities;

/**
 * Sous-type de jeu pour les activités {@link ActivityType#GAME} (API {@code type: "GAME"}).
 * <ul>
 *   <li>{@link #MEMORY_QUIZ} — QCM texte (mémoire)</li>
 *   <li>{@link #IMAGE_RECOGNITION} — reconnaissance visuelle (une URL d’image par question)</li>
 *   <li>{@link #PUZZLE} — puzzle image (réutilise temporairement le moteur MEMORY_MATCH)</li>
 * </ul>
 * Null pour CONTENT / VIDEO.
 */
public enum GameType {
    MEMORY_QUIZ,
    IMAGE_RECOGNITION,
    /**
     * Memory / paires : deux cartes par paire (même {@code correctAnswer} = identifiant de paire),
     * une image par carte ({@code imageUrl}).
     */
    MEMORY_MATCH,
    /** Puzzle image (jigsaw) — branché provisoirement sur la logique memory/paires. */
    PUZZLE
}
