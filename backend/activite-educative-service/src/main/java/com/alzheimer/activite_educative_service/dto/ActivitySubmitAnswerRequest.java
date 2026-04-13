package com.alzheimer.activite_educative_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corps pour {@code POST /api/activities/{activityId}/submit-answer}.
 */
public class ActivitySubmitAnswerRequest {

    @NotNull
    private Long sessionId;

    @NotNull
    private Long questionId;

    @NotBlank
    @Size(max = 1000)
    private String answer;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
