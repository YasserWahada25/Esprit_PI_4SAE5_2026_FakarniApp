package com.alzheimer.Post_Service.dto;

public class CommentRequest {
    private String userId;
    private String content;
    private Long parentCommentId;

    public CommentRequest() {
    }

    public CommentRequest(String userId, String content, Long parentCommentId) {
        this.userId = userId;
        this.content = content;
        this.parentCommentId = parentCommentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
}

