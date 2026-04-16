package com.alzheimer.group_service.controllers;

import com.alzheimer.group_service.dto.GroupRequest;
import com.alzheimer.group_service.dto.GroupResponse;
import com.alzheimer.group_service.services.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GroupControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        GroupController controller = new GroupController();
        ReflectionTestUtils.setField(controller, "groupService", groupService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createGroup_returnsCreated() throws Exception {
        GroupResponse response = new GroupResponse();
        response.setId(1L);
        when(groupService.createGroup(any(GroupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GroupRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void getGroupById_returnsOk() throws Exception {
        when(groupService.getGroupById(eq(1L), eq(true))).thenReturn(new GroupResponse());

        mockMvc.perform(get("/api/groups/1").param("includeMembers", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllGroups_returnsOk() throws Exception {
        when(groupService.getAllGroups(eq(false))).thenReturn(List.of(new GroupResponse()));

        mockMvc.perform(get("/api/groups").param("includeMembers", "false"))
                .andExpect(status().isOk());
    }
}
