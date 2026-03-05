package com.alzheimer.group_service.dto;

import com.alzheimer.group_service.entities.MemberRole;
import java.time.LocalDateTime;

public class GroupMemberResponse {

    private Long id;
    private Long userId;
    private MemberRole role;
    private LocalDateTime joinedAt;
    private Long invitedBy;

    public GroupMemberResponse() {
    }

    public GroupMemberResponse(Long id, Long userId, MemberRole role, LocalDateTime joinedAt, Long invitedBy) {
        this.id = id;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
        this.invitedBy = invitedBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Long getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(Long invitedBy) {
        this.invitedBy = invitedBy;
    }
}
