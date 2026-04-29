package com.alzheimer.Post_Service.dto;

import com.alzheimer.Post_Service.entities.Reaction.ReactionType;

public class ReactionRequest {
    private Long userId;
    private ReactionType type;

    public ReactionRequest() {
    }

    public ReactionRequest(Long userId, ReactionType type) {
        this.userId = userId;
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ReactionType getType() {
        return type;
    }

    public void setType(ReactionType type) {
        this.type = type;
    }
}

