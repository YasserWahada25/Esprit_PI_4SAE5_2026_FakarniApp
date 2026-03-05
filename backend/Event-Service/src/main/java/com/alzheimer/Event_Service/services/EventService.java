package com.alzheimer.Event_Service.services;

import com.alzheimer.Event_Service.dto.EventCreateRequest;
import com.alzheimer.Event_Service.dto.EventResponse;
import com.alzheimer.Event_Service.entities.Event;
import com.alzheimer.Event_Service.repositories.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EmailService emailService;

    public EventService(EventRepository eventRepository, EmailService emailService) {
        this.eventRepository = eventRepository;
        this.emailService = emailService;
    }

    // Création d'un événement
    public EventResponse create(EventCreateRequest request) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDateTime(request.getStartDateTime());
        event.setLocation(request.getLocation());
        event.setRemindEnabled(request.isRemindEnabled());
        event.setUserId(request.getUserId());
        event.setLat(request.getLat());
        event.setLng(request.getLng());
        // createdAt is set automatically via @PrePersist

        Event savedEvent = eventRepository.save(event);
        
        // Envoi d'email statique immédiat si le rappel est activé
        if (savedEvent.isRemindEnabled()) {
            emailService.sendImmediateReminderToStaticUser(savedEvent);
        }

        return new EventResponse(savedEvent);
    }

    // Mise à jour d'un événement
    public EventResponse update(Long id, EventCreateRequest request) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDateTime(request.getStartDateTime());
        event.setLocation(request.getLocation());
        event.setRemindEnabled(request.isRemindEnabled());
        event.setUserId(request.getUserId());
        event.setLat(request.getLat());
        event.setLng(request.getLng());
        // createdAt must NOT be modified (updatable = false)

        Event updatedEvent = eventRepository.save(event);

        // Envoi d'email statique immédiat si le rappel est toujours activé
        if (updatedEvent.isRemindEnabled()) {
            emailService.sendImmediateReminderToStaticUser(updatedEvent);
        }

        return new EventResponse(updatedEvent);
    }

    // Récupérer un événement par ID
    public EventResponse getById(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
        return new EventResponse(event);
    }

    // Récupérer tous les événements
    public List<EventResponse> getAll() {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());
    }

    // Supprimer un événement
    public void delete(Long id) {
        eventRepository.deleteById(id);
    }
}