package com.alzheimer.activite_educative_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionAnswerDetailDto {

    private Long questionId;

    /** Intitulé de la question (rappel pour le récapitulatif). */
    private String prompt;

    /** Réponse attendue (corrigé). */
    @JsonProperty("expectedAnswer")
    @JsonAlias({"expected_answer", "correct_answer", "correctAnswer"})
    private String expectedAnswer;

    @JsonProperty("imageUrl")
    @JsonAlias({"image_url"})
    private String imageUrl;

    @JsonProperty("questionImageUrl")
    @JsonAlias({"question_image_url"})
    private String questionImageUrl;

    private String userAnswer;
    private boolean correct;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }

    public void setExpectedAnswer(String expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
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

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
