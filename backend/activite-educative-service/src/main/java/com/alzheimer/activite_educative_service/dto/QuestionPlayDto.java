package com.alzheimer.activite_educative_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

/** Question exposée pendant une session (sans la bonne réponse). */
public class QuestionPlayDto {

    private Long id;
    private Integer orderIndex;
    private String prompt;

    @JsonAlias({"image_url", "IMAGE_URL"})
    private String imageUrl;
    /** Alias JSON pour l’image de la question (même valeur que {@link #imageUrl}). */
    @JsonAlias({"question_image_url", "QUESTION_IMAGE_URL"})
    private String questionImageUrl;
    private List<String> options;
    /** Options riches (label + image optionnelle) si le JSON en base est un tableau d’objets. */
    private List<QuizOptionPlayDto> quizOptions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getQuestionImageUrl() {
        return questionImageUrl;
    }

    public void setQuestionImageUrl(String questionImageUrl) {
        this.questionImageUrl = questionImageUrl;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<QuizOptionPlayDto> getQuizOptions() {
        return quizOptions;
    }

    public void setQuizOptions(List<QuizOptionPlayDto> quizOptions) {
        this.quizOptions = quizOptions;
    }
}
