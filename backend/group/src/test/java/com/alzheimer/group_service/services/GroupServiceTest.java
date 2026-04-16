package com.alzheimer.group_service.services;

import com.alzheimer.group_service.client.UserClient;
import com.alzheimer.group_service.dto.AddMemberRequest;
import com.alzheimer.group_service.dto.GroupRequest;
import com.alzheimer.group_service.dto.GroupResponse;
import com.alzheimer.group_service.entities.Group;
import com.alzheimer.group_service.entities.GroupType;
import com.alzheimer.group_service.entities.MemberRole;
import com.alzheimer.group_service.repositories.GroupMemberRepository;
import com.alzheimer.group_service.repositories.GroupRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    @Mock
    private UserClient userClient;

    @InjectMocks
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("creator-1", "pwd")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createGroup_success_createsGroupAndCreatorMember() {
        GroupRequest request = new GroupRequest();
        request.setName("Support Group");
        request.setDescription("desc");
        request.setGroupType(GroupType.PUBLIC);
        request.setIsJoinable(true);

        Group saved = new Group();
        saved.setId(10L);
        saved.setName("Support Group");
        saved.setDescription("desc");
        saved.setCreatorId("creator-1");
        when(groupRepository.save(any(Group.class))).thenReturn(saved);

        GroupResponse result = groupService.createGroup(request);

        assertEquals(10L, result.getId());
        verify(groupRepository).save(any(Group.class));
        verify(groupMemberRepository).save(any());
    }

    @Test
    void addMember_whenGroupNotFound_throwsException() {
        AddMemberRequest request = new AddMemberRequest();
        request.setUserId("u2");
        request.setRole(MemberRole.MEMBER);
        when(groupRepository.findById(77L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> groupService.addMember(77L, request));

        assertEquals("Group not found with id: 77", ex.getMessage());
        verify(groupMemberRepository, never()).save(any());
    }
}
