package com.alzheimer.Event_Service.controllers;

import com.alzheimer.Event_Service.dto.EventCreateRequest;
import com.alzheimer.Event_Service.dto.EventParticipationRequest;
import com.alzheimer.Event_Service.dto.EventParticipationResponse;
import com.alzheimer.Event_Service.dto.EventResponse;
import com.alzheimer.Event_Service.entities.EventParticipationStatus;
import com.alzheimer.Event_Service.services.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public EventResponse create(@RequestBody EventCreateRequest request) {
        return eventService.create(request);
    }

    @PutMapping("/{id}")
    public EventResponse update(@PathVariable Long id, @RequestBody EventCreateRequest request) {
        return eventService.update(id, request);
    }

    @GetMapping("/{id}")
    public EventResponse getById(@PathVariable Long id) {
        return eventService.getById(id);
    }

    @GetMapping
    public List<EventResponse> getAll() {
        return eventService.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        eventService.delete(id);
    }

    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        return eventService.uploadCoverImage(file);
    }

    @PostMapping("/{eventId}/participations")
    public EventParticipationResponse registerParticipation(
            @PathVariable Long eventId,
            @Valid @RequestBody EventParticipationRequest request
    ) {
        return eventService.registerParticipation(eventId, request);
    }

    @PatchMapping("/participations/{participationId}/status")
    public EventParticipationResponse updateParticipationStatus(
            @PathVariable Long participationId,
            @RequestParam EventParticipationStatus status
    ) {
        return eventService.updateParticipationStatus(participationId, status);
    }

    /** AgrÃ©gation suivi engagement : toutes les participations ou filtrÃ©es par patient. */
    @GetMapping("/participations")
    public List<EventParticipationResponse> listParticipations(
            @RequestParam(required = false) String patientId
    ) {
        return eventService.listParticipationsForEngagement(patientId);
    }
}
