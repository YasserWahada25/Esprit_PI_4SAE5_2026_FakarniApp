package com.alzheimer.activite_educative_service.dto;

import com.alzheimer.activite_educative_service.entities.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

public class GameSessionResultResponse {

    private Long sessionId;
    private Long activityId;
    private String activityTitle;
    private Long userId;
    private SessionStatus status;
    private Integer totalQuestions;
    private Integer correctCount;
    private Double scorePercent;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private List<SessionAnswerDetailDto> answers;

    /** Alias métier : nombre de bonnes réponses (= {@link #correctCount}). */
    private Integer score;
    /** Alias métier : nombre total de questions (= {@link #totalQuestions}). */
    private Integer scoreMax;
    /** Alias métier : pourcentage de réussite (= {@link #scorePercent}). */
    private Double percentage;
    /** Alias métier : fin de partie (= {@link #finishedAt}). */
    private LocalDateTime dateCompleted;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
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

    public List<SessionAnswerDetailDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SessionAnswerDetailDto> answers) {
        this.answers = answers;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getScoreMax() {
        return scoreMax;
    }

    public void setScoreMax(Integer scoreMax) {
        this.scoreMax = scoreMax;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public LocalDateTime getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(LocalDateTime dateCompleted) {
        this.dateCompleted = dateCompleted;
    }
}
