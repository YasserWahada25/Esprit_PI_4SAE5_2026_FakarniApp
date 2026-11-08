package com.alzheimer.Post_Service.dto;

import com.alzheimer.Post_Service.entities.Reaction.ReactionType;

public class ReactionRequest {
    private String userId;
    private ReactionType type;

    public ReactionRequest() {
    }

    public ReactionRequest(String userId, ReactionType type) {
        this.userId = userId;
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ReactionType getType() {
        return type;
    }

    public void setType(ReactionType type) {
        this.type = type;
    }
}

