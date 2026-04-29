package com.alzheimer.activite_educative_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionRequest {

    @NotNull
    private Integer orderIndex;

    @NotBlank
    @Size(max = 2000)
    @JsonAlias({"questionText", "question_text"})
    private String prompt;

    @Size(max = 2000)
    @JsonAlias({"questionImageUrl", "question_image_url"})
    private String imageUrl;

    /** Vide autorisé pour les cartes MEMORY_MATCH. */
    @NotNull
    @Size(min = 0, max = 20)
    private List<@NotBlank @Size(max = 500) String> options;

    @NotBlank
    @Size(max = 500)
    private String correctAnswer;

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

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
