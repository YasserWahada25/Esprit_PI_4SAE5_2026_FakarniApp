package com.alzheimer.activite_educative_service.dto;

import com.alzheimer.activite_educative_service.entities.GameType;
import com.alzheimer.activite_educative_service.entities.SessionStatus;

import java.util.List;

public class GameSessionStartResponse {

    private Long sessionId;
    private Long activityId;
    private String patientId;
    private SessionStatus status;
    private Integer totalQuestions;
    /** {@link GameType#MEMORY_QUIZ} ou {@link GameType#IMAGE_RECOGNITION} pour l’activité lancée. */
    private GameType gameType;
    private List<QuestionPlayDto> questions;
    /** Renseigné pour {@link GameType#MEMORY_MATCH} (paires mémoire). */
    private List<ImageCardPlayDto> imageCards;

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

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
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

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public List<QuestionPlayDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionPlayDto> questions) {
        this.questions = questions;
    }

    public List<ImageCardPlayDto> getImageCards() {
        return imageCards;
    }

    public void setImageCards(List<ImageCardPlayDto> imageCards) {
        this.imageCards = imageCards;
    }
}
