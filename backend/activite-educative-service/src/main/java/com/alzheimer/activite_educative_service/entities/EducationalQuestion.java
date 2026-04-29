package com.alzheimer.activite_educative_service.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "educational_questions")
public class EducationalQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private ActiviteEducative activity;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false, length = 2000)
    private String prompt;

    /** URL d’image pour la reconnaissance d’images (optionnel). */
    @Column(length = 2000)
    private String imageUrl;

    /**
     * Options de réponse au format JSON : tableau de chaînes, ex. ["Pomme","Banane","Orange"].
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionsJson;

    /** Réponse attendue (comparaison insensible à la casse après trim). */
    @Column(nullable = false, length = 500)
    private String correctAnswer;

    @Column(length = 2000)
    private String explanation;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActiviteEducative getActivity() {
        return activity;
    }

    public void setActivity(ActiviteEducative activity) {
        this.activity = activity;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
