package com.alzheimer.group_service.services;

import com.alzheimer.group_service.dto.AddMemberRequest;
import com.alzheimer.group_service.dto.GroupMemberResponse;
import com.alzheimer.group_service.dto.GroupRequest;
import com.alzheimer.group_service.dto.GroupResponse;
import com.alzheimer.group_service.entities.*;
import com.alzheimer.group_service.repositories.GroupMemberRepository;
import com.alzheimer.group_service.repositories.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Transactional
    public GroupResponse createGroup(GroupRequest request) {
        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatorId(request.getCreatorId());
        group.setGroupType(request.getGroupType());
        group.setCoverImageUrl(request.getCoverImageUrl());
        group.setMaxMembers(request.getMaxMembers());
        group.setIsJoinable(request.getIsJoinable());

        Group savedGroup = groupRepository.save(group);

        // Ajouter le créateur comme ADMIN
        GroupMember creatorMember = new GroupMember(savedGroup, request.getCreatorId(), MemberRole.ADMIN);
        groupMemberRepository.save(creatorMember);

        return toResponse(savedGroup, true);
    }

    public GroupResponse getGroupById(Long id, boolean includeMembers) {
        Optional<Group> groupOptional = groupRepository.findById(id);
        if (groupOptional.isEmpty()) {
            throw new RuntimeException("Group not found with id: " + id);
        }
        return toResponse(groupOptional.get(), includeMembers);
    }

    public List<GroupResponse> getAllGroups(boolean includeMembers) {
        Iterable<Group> groups = groupRepository.findAll();
        List<GroupResponse> responses = new java.util.ArrayList<>();

        for (Group group : groups) {
            responses.add(toResponse(group, includeMembers));
        }

        return responses;
    }

    @Transactional
    public GroupResponse updateGroup(Long id, GroupRequest request) {
        Optional<Group> groupOptional = groupRepository.findById(id);
        if (groupOptional.isEmpty()) {
            throw new RuntimeException("Group not found with id: " + id);
        }

        Group group = groupOptional.get();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        
        if (request.getGroupType() != null) {
            group.setGroupType(request.getGroupType());
        }
        if (request.getCoverImageUrl() != null) {
            group.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getMaxMembers() != null) {
            group.setMaxMembers(request.getMaxMembers());
        }
        if (request.getIsJoinable() != null) {
            group.setIsJoinable(request.getIsJoinable());
        }

        Group updatedGroup = groupRepository.save(group);
        return toResponse(updatedGroup, true);
    }

    @Transactional
    public void deleteGroup(Long id) {
        Optional<Group> groupOptional = groupRepository.findById(id);
        if (groupOptional.isEmpty()) {
            throw new RuntimeException("Group not found with id: " + id);
        }
        groupRepository.deleteById(id);
    }

    // Gestion des membres
    @Transactional
    public GroupMemberResponse addMember(Long groupId, AddMemberRequest request) {
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        if (groupOptional.isEmpty()) {
            throw new RuntimeException("Group not found with id: " + groupId);
        }

        Group group = groupOptional.get();

        // Vérifier si l'utilisateur est déjà membre
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, request.getUserId())) {
            throw new RuntimeException("User is already a member of this group");
        }

        // Vérifier la limite de membres
        if (group.getMaxMembers() != null) {
            long currentMemberCount = groupMemberRepository.countByGroupId(groupId);
            if (currentMemberCount >= group.getMaxMembers()) {
                throw new RuntimeException("Group has reached maximum member capacity");
            }
        }

        GroupMember member = new GroupMember(group, request.getUserId(), request.getRole());
        member.setInvitedBy(request.getInvitedBy());
        GroupMember savedMember = groupMemberRepository.save(member);

        return toMemberResponse(savedMember);
    }

    @Transactional
    public void removeMember(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new RuntimeException("User is not a member of this group");
        }
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Transactional
    public GroupMemberResponse updateMemberRole(Long groupId, Long userId, MemberRole newRole) {
        Optional<GroupMember> memberOptional = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        if (memberOptional.isEmpty()) {
            throw new RuntimeException("Member not found");
        }

        GroupMember member = memberOptional.get();
        member.setRole(newRole);
        GroupMember updatedMember = groupMemberRepository.save(member);

        return toMemberResponse(updatedMember);
    }

    public List<GroupMemberResponse> getGroupMembers(Long groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return members.stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    public List<GroupResponse> getUserGroups(Long userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(membership -> toResponse(membership.getGroup(), false))
                .collect(Collectors.toList());
    }

    // Méthodes de conversion
    private GroupResponse toResponse(Group group, boolean includeMembers) {
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setCreatorId(group.getCreatorId());
        response.setGroupType(group.getGroupType());
        response.setStatus(group.getStatus());
        response.setCoverImageUrl(group.getCoverImageUrl());
        response.setMaxMembers(group.getMaxMembers());
        response.setIsJoinable(group.getIsJoinable());
        response.setMemberCount(group.getMemberCount());
        response.setCreatedAt(group.getCreatedAt());
        response.setUpdatedAt(group.getUpdatedAt());

        if (includeMembers) {
            List<GroupMemberResponse> memberResponses = group.getMembers().stream()
                    .map(this::toMemberResponse)
                    .collect(Collectors.toList());
            response.setMembers(memberResponses);
        }

        return response;
    }

    private GroupMemberResponse toMemberResponse(GroupMember member) {
        return new GroupMemberResponse(
                member.getId(),
                member.getUserId(),
                member.getRole(),
                member.getJoinedAt(),
                member.getInvitedBy()
        );
    }
}
