package com.alzheimer.post_service.dto;

public class CommentRequest {
    private Long userId;
    private String content;
    private Long parentCommentId;

    public CommentRequest() {
    }

    public CommentRequest(Long userId, String content, Long parentCommentId) {
        this.userId = userId;
        this.content = content;
        this.parentCommentId = parentCommentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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
