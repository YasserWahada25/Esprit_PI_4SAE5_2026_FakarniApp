package com.alzheimer.group_service.dto;

import com.alzheimer.group_service.entities.MemberRole;

public class AddMemberRequest {

    private Long userId;
    private MemberRole role = MemberRole.MEMBER;
    private Long invitedBy;

    public AddMemberRequest() {
    }

    public AddMemberRequest(Long userId, MemberRole role, Long invitedBy) {
        this.userId = userId;
        this.role = role;
        this.invitedBy = invitedBy;
    }

    // Getters and Setters
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

    public Long getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(Long invitedBy) {
        this.invitedBy = invitedBy;
    }
}
