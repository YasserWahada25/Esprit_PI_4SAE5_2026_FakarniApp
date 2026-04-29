package com.alzheimer.activite_educative_service.dto;

public class SubmitAnswerResponse {

    private boolean correct;
    private int correctAnswersSoFar;
    private int answeredCount;

    /** {@code true} si la partie vient d’être clôturée (toutes les questions répondues). */
    private boolean sessionFinished;

    /** Présent si {@link #sessionFinished} : résultat persistant (SUCCESS / FAILURE, score, etc.). */
    private GameSessionResultResponse sessionResult;

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getCorrectAnswersSoFar() {
        return correctAnswersSoFar;
    }

    public void setCorrectAnswersSoFar(int correctAnswersSoFar) {
        this.correctAnswersSoFar = correctAnswersSoFar;
    }

    public int getAnsweredCount() {
        return answeredCount;
    }

    public void setAnsweredCount(int answeredCount) {
        this.answeredCount = answeredCount;
    }

    public boolean isSessionFinished() {
        return sessionFinished;
    }

    public void setSessionFinished(boolean sessionFinished) {
        this.sessionFinished = sessionFinished;
    }

    public GameSessionResultResponse getSessionResult() {
        return sessionResult;
    }

    public void setSessionResult(GameSessionResultResponse sessionResult) {
        this.sessionResult = sessionResult;
    }
}
