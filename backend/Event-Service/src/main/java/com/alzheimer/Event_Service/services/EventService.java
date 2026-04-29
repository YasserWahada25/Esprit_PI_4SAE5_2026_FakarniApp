package com.alzheimer.Event_Service.services;

import com.alzheimer.Event_Service.dto.EventCreateRequest;
import com.alzheimer.Event_Service.dto.EventParticipationRequest;
import com.alzheimer.Event_Service.dto.EventParticipationResponse;
import com.alzheimer.Event_Service.dto.EventResponse;
import com.alzheimer.Event_Service.entities.Event;
import com.alzheimer.Event_Service.entities.EventParticipation;
import com.alzheimer.Event_Service.entities.EventParticipationStatus;
import com.alzheimer.Event_Service.repositories.EventParticipationRepository;
import com.alzheimer.Event_Service.repositories.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {
    private static final String UPLOAD_DIR = "uploads/events/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String PUBLIC_UPLOAD_BASE_URL = "http://localhost:8087/uploads/events/";

    private final EventRepository eventRepository;
    private final EventParticipationRepository participationRepository;
    private final EmailService emailService;

    public EventService(
            EventRepository eventRepository,
            EventParticipationRepository participationRepository,
            EmailService emailService
    ) {
        this.eventRepository = eventRepository;
        this.participationRepository = participationRepository;
        this.emailService = emailService;
    }

    // CrÃ©ation d'un Ã©vÃ©nement
    public EventResponse create(EventCreateRequest request) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDateTime(request.getStartDateTime());
        event.setLocation(request.getLocation());
        event.setCoverImageUrl(request.getCoverImageUrl());
        event.setRemindEnabled(request.isRemindEnabled());
        event.setUserId(request.getUserId());
        event.setLat(request.getLat());
        event.setLng(request.getLng());
        // createdAt is set automatically via @PrePersist

        Event savedEvent = eventRepository.save(event);
        
        // Envoi d'email statique immÃ©diat si le rappel est activÃ©
        if (savedEvent.isRemindEnabled()) {
            emailService.sendImmediateReminderToStaticUser(savedEvent);
        }

        return new EventResponse(savedEvent);
    }

    // Mise Ã  jour d'un Ã©vÃ©nement
    public EventResponse update(Long id, EventCreateRequest request) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDateTime(request.getStartDateTime());
        event.setLocation(request.getLocation());
        event.setCoverImageUrl(request.getCoverImageUrl());
        event.setRemindEnabled(request.isRemindEnabled());
        event.setUserId(request.getUserId());
        event.setLat(request.getLat());
        event.setLng(request.getLng());
        // createdAt must NOT be modified (updatable = false)

        Event updatedEvent = eventRepository.save(event);

        // Envoi d'email statique immÃ©diat si le rappel est toujours activÃ©
        if (updatedEvent.isRemindEnabled()) {
            emailService.sendImmediateReminderToStaticUser(updatedEvent);
        }

        return new EventResponse(updatedEvent);
    }

    // RÃ©cupÃ©rer un Ã©vÃ©nement par ID
    public EventResponse getById(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
        return new EventResponse(event);
    }

    // RÃ©cupÃ©rer tous les Ã©vÃ©nements
    public List<EventResponse> getAll() {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());
    }

    // Supprimer un Ã©vÃ©nement
    public void delete(Long id) {
        participationRepository.deleteAllByEvent_Id(id);
        eventRepository.deleteById(id);
    }

    public EventParticipationResponse registerParticipation(Long eventId, EventParticipationRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        String pid = request.getPatientId().trim();
        if (participationRepository.existsByEvent_IdAndPatientId(eventId, pid)) {
            throw new IllegalStateException("Patient already registered for this event");
        }
        EventParticipation p = new EventParticipation();
        p.setEvent(event);
        p.setPatientId(pid);
        p.setStatus(EventParticipationStatus.REGISTERED);
        EventParticipation saved = participationRepository.save(p);
        return new EventParticipationResponse(saved, progressForStatus(saved.getStatus()));
    }

    public EventParticipationResponse updateParticipationStatus(Long participationId, EventParticipationStatus status) {
        EventParticipation p = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));
        p.setStatus(status);
        EventParticipation saved = participationRepository.save(p);
        return new EventParticipationResponse(saved, progressForStatus(saved.getStatus()));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<EventParticipationResponse> listParticipationsForEngagement(String patientId) {
        List<EventParticipation> rows = StringUtils.hasText(patientId)
                ? participationRepository.findByPatientIdOrderByRegisteredAtDesc(patientId.trim())
                : participationRepository.findAllByOrderByRegisteredAtDesc();
        return rows.stream()
                .map(p -> new EventParticipationResponse(p, progressForStatus(p.getStatus())))
                .collect(Collectors.toList());
    }

    public ResponseEntity<Map<String, String>> uploadCoverImage(MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        try {
            if (file == null || file.isEmpty()) {
                response.put("error", "Le fichier est vide");
                return ResponseEntity.badRequest().body(response);
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                response.put("error", "Le fichier est trop volumineux (max 5MB)");
                return ResponseEntity.badRequest().body(response);
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("error", "Le fichier doit Ãªtre une image");
                return ResponseEntity.badRequest().body(response);
            }

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + extension;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            response.put("url", PUBLIC_UPLOAD_BASE_URL + fileName);
            response.put("message", "Image uploadee avec succes");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("error", "Erreur lors de l'upload: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private static int progressForStatus(EventParticipationStatus s) {
        if (s == null) {
            return 0;
        }
        return switch (s) {
            case REGISTERED -> 25;
            case ATTENDED -> 75;
            case TERMINATED -> 100;
            case CANCELLED -> 0;
        };
    }
}
