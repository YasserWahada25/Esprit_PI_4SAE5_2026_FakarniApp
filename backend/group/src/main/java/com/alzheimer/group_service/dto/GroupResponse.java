package com.alzheimer.group_service.dto;

import com.alzheimer.group_service.entities.GroupStatus;
import com.alzheimer.group_service.entities.GroupType;
import java.time.LocalDateTime;
import java.util.List;

public class GroupResponse {

    private Long id;
    private String name;
    private String description;
    private Long creatorId;
    private GroupType groupType;
    private GroupStatus status;
    private String coverImageUrl;
    private Integer maxMembers;
    private Boolean isJoinable;
    private Integer memberCount;
    private List<GroupMemberResponse> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public GroupResponse() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
    }

    public GroupStatus getStatus() {
        return status;
    }

    public void setStatus(GroupStatus status) {
        this.status = status;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public Boolean getIsJoinable() {
        return isJoinable;
    }

    public void setIsJoinable(Boolean isJoinable) {
        this.isJoinable = isJoinable;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public List<GroupMemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMemberResponse> members) {
        this.members = members;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
