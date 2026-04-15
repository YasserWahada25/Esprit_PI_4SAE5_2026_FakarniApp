package com.alzheimer.group_service.dto;

import com.alzheimer.group_service.entities.GroupType;

public class GroupRequest {

    private String name;
    private String description;
    private Long creatorId;
    private GroupType groupType = GroupType.PUBLIC;
    private String coverImageUrl;
    private Integer maxMembers;
    private Boolean isJoinable = true;

    // Constructors
    public GroupRequest() {
    }

    public GroupRequest(String name, String description, Long creatorId) {
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
    }

    // Getters and Setters
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
}
