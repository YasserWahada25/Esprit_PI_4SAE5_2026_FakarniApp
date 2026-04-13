package com.alzheimer.activite_educative_service.dto;

import jakarta.validation.constraints.NotNull;

public class GameSessionStartRequest {

    @NotNull
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
