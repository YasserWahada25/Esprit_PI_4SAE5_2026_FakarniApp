package com.alzheimer.activite_educative_service.dto;

public class MemoryMoveResponse {

    private boolean match;
    private int movesCount;
    private int pairsFound;
    private int pairTotal;
    private boolean gameCompleted;
    private GameSessionResultResponse sessionResult;

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public int getMovesCount() {
        return movesCount;
    }

    public void setMovesCount(int movesCount) {
        this.movesCount = movesCount;
    }

    public int getPairsFound() {
        return pairsFound;
    }

    public void setPairsFound(int pairsFound) {
        this.pairsFound = pairsFound;
    }

    public int getPairTotal() {
        return pairTotal;
    }

    public void setPairTotal(int pairTotal) {
        this.pairTotal = pairTotal;
    }

    public boolean isGameCompleted() {
        return gameCompleted;
    }

    public void setGameCompleted(boolean gameCompleted) {
        this.gameCompleted = gameCompleted;
    }

    public GameSessionResultResponse getSessionResult() {
        return sessionResult;
    }

    public void setSessionResult(GameSessionResultResponse sessionResult) {
        this.sessionResult = sessionResult;
    }
}
