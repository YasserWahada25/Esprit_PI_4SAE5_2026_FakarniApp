package com.alzheimer.activite_educative_service.dto;

import com.alzheimer.activite_educative_service.entities.EducationalQuestion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class QuestionResponse {

    private Long id;
    private Long activityId;
    private Integer orderIndex;
    private String prompt;
    private String imageUrl;
    private List<String> options;
    private String correctAnswer;
    private LocalDateTime createdAt;

    public QuestionResponse() {
    }

    public QuestionResponse(EducationalQuestion q, ObjectMapper objectMapper) {
        this.id = q.getId();
        this.activityId = q.getActivity().getId();
        this.orderIndex = q.getOrderIndex();
        this.prompt = q.getPrompt();
        this.imageUrl = q.getImageUrl();
        this.correctAnswer = q.getCorrectAnswer();
        this.createdAt = q.getCreatedAt();
        this.options = parseOptions(q.getOptionsJson(), objectMapper);
    }

    public static QuestionResponse withoutCorrectAnswer(EducationalQuestion q, ObjectMapper objectMapper) {
        QuestionResponse r = new QuestionResponse(q, objectMapper);
        r.setCorrectAnswer(null);
        return r;
    }

    private static List<String> parseOptions(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
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

    /** Alias JSON de {@link #prompt} pour les clients qui attendent {@code questionText}. */
    public String getQuestionText() {
        return prompt;
    }

    public void setQuestionText(String questionText) {
        this.prompt = questionText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /** Alias JSON de {@link #imageUrl} pour les clients qui attendent {@code questionImageUrl}. */
    public String getQuestionImageUrl() {
        return imageUrl;
    }

    public void setQuestionImageUrl(String questionImageUrl) {
        this.imageUrl = questionImageUrl;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
