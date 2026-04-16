package com.alzheimer.group_service.dto;

import com.alzheimer.group_service.entities.MemberRole;

public class AddMemberRequest {

    private String userId;
    private MemberRole role = MemberRole.MEMBER;
    private String invitedBy;

    public AddMemberRequest() {
    }

    public AddMemberRequest(String userId, MemberRole role, String invitedBy) {
        this.userId = userId;
        this.role = role;
        this.invitedBy = invitedBy;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }
}
