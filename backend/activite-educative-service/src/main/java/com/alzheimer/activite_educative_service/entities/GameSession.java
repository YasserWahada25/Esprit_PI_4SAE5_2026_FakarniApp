package com.alzheimer.activite_educative_service.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifiant patient côté microservice User (MongoDB / JWT).
     * Nullable pour compatibilité des lignes historiques (avant migration {@code patient_id}).
     */
    @Column(name = "patient_id", length = 64)
    private String patientId;

    /**
     * Ancienne colonne {@code user_id} (BIGINT, souvent NOT NULL en base). Conservée pour ne pas
     * casser le schéma existant : remplie avec 0 si l’identifiant patient n’est pas un entier.
     */
    @Column(name = "user_id", nullable = false)
    private Long legacyUserId = 0L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private ActiviteEducative activity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SessionStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    /** Nombre total de questions au démarrage (figé). */
    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer correctCount;

    /** Score final en pourcentage (0–100), renseigné à la clôture. */
    private Double scorePercent;

    /** Nombre de tentatives de paire (jeu MEMORY_MATCH). */
    private Integer movesCount;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (correctCount == null) {
            correctCount = 0;
        }
        legacyUserId = parseLegacyUserId(patientId);
    }

    private static long parseLegacyUserId(String pid) {
        if (pid == null || pid.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(pid.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientId() {
        if (patientId != null && !patientId.isBlank()) {
            return patientId;
        }
        if (legacyUserId != null && legacyUserId != 0L) {
            return String.valueOf(legacyUserId);
        }
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public Long getLegacyUserId() {
        return legacyUserId;
    }

    public void setLegacyUserId(Long legacyUserId) {
        this.legacyUserId = legacyUserId != null ? legacyUserId : 0L;
    }

    public ActiviteEducative getActivity() {
        return activity;
    }

    public void setActivity(ActiviteEducative activity) {
        this.activity = activity;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Double getScorePercent() {
        return scorePercent;
    }

    public void setScorePercent(Double scorePercent) {
        this.scorePercent = scorePercent;
    }

    public Integer getMovesCount() {
        return movesCount;
    }

    public void setMovesCount(Integer movesCount) {
        this.movesCount = movesCount;
    }
}
