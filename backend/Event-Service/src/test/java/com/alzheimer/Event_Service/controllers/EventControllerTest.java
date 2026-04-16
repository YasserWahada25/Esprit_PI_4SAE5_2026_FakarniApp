package com.alzheimer.event_service.controllers;

import com.alzheimer.event_service.dto.EventCreateRequest;
import com.alzheimer.event_service.dto.EventResponse;
import com.alzheimer.event_service.services.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new EventController(eventService)).build();
    }

    @Test
    void create_returnsOk() throws Exception {
        when(eventService.create(any(EventCreateRequest.class))).thenReturn(
                new EventResponse(1L, "title", "desc", null, "loc", false, 10L, null)
        );

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EventCreateRequest())))
                .andExpect(status().isOk());
    }

    @Test
    void getById_returnsOk() throws Exception {
        when(eventService.getById(eq(1L))).thenReturn(
                new EventResponse(1L, "title", "desc", null, "loc", false, 10L, null)
        );

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_returnsOk() throws Exception {
        when(eventService.getAll()).thenReturn(
                List.of(new EventResponse(1L, "title", "desc", null, "loc", false, 10L, null))
        );

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk());
    }
}
