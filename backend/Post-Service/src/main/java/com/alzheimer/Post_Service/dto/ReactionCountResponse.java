package com.alzheimer.post_service.dto;

import java.util.Map;

public class ReactionCountResponse {
    private Map<String, Long> counts;
    private String userReaction;

    public ReactionCountResponse() {
    }

    public ReactionCountResponse(Map<String, Long> counts, String userReaction) {
        this.counts = counts;
        this.userReaction = userReaction;
    }

    public Map<String, Long> getCounts() {
        return counts;
    }

    public void setCounts(Map<String, Long> counts) {
        this.counts = counts;
    }

    public String getUserReaction() {
        return userReaction;
    }

    public void setUserReaction(String userReaction) {
        this.userReaction = userReaction;
    }
}
