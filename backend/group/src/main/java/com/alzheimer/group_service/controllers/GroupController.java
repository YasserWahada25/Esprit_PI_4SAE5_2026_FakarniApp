package com.alzheimer.group_service.controllers;

import com.alzheimer.group_service.dto.AddMemberRequest;
import com.alzheimer.group_service.dto.GroupMemberResponse;
import com.alzheimer.group_service.dto.GroupRequest;
import com.alzheimer.group_service.dto.GroupResponse;
import com.alzheimer.group_service.entities.MemberRole;
import com.alzheimer.group_service.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody GroupRequest request) {
        GroupResponse response = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroupById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeMembers) {
        GroupResponse response = groupService.getGroupById(id, includeMembers);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups(
            @RequestParam(defaultValue = "false") boolean includeMembers) {
        List<GroupResponse> responses = groupService.getAllGroups(includeMembers);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponse> updateGroup(@PathVariable Long id, @RequestBody GroupRequest request) {
        GroupResponse response = groupService.updateGroup(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoints pour la gestion des membres
    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupMemberResponse> addMember(
            @PathVariable Long groupId,
            @RequestBody AddMemberRequest request) {
        GroupMemberResponse response = groupService.addMember(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{groupId}/members/{userId}/role")
    public ResponseEntity<GroupMemberResponse> updateMemberRole(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestBody Map<String, String> roleUpdate) {
        MemberRole newRole = MemberRole.valueOf(roleUpdate.get("role"));
        GroupMemberResponse response = groupService.updateMemberRole(groupId, userId, newRole);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMemberResponse> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupResponse>> getUserGroups(@PathVariable Long userId) {
        List<GroupResponse> groups = groupService.getUserGroups(userId);
        return ResponseEntity.ok(groups);
    }
}
