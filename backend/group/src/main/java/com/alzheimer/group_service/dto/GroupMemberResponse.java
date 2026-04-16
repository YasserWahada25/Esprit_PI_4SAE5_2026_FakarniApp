package com.alzheimer.group_service.dto;

import com.alzheimer.group_service.entities.MemberRole;
import java.time.LocalDateTime;

public class GroupMemberResponse {

    private Long id;
    private String userId;
    private UserDTO user;
    private MemberRole role;
    private LocalDateTime joinedAt;
    private String invitedBy;

    public GroupMemberResponse() {
    }

    public GroupMemberResponse(Long id, String userId, MemberRole role, LocalDateTime joinedAt, String invitedBy) {
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
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

    public String getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }
}
