package com.alzheimer.session_service.services;

import com.alzheimer.session_service.dto.AddParticipantRequest;
import com.alzheimer.session_service.dto.CreateSessionRequest;
import com.alzheimer.session_service.dto.UpdateParticipantPrefsRequest;
import com.alzheimer.session_service.dto.UpdateParticipantStatusRequest;
import com.alzheimer.session_service.dto.UpdateSessionRequest;
import com.alzheimer.session_service.entities.JoinStatus;
import com.alzheimer.session_service.entities.MeetingMode;
import com.alzheimer.session_service.entities.ParticipantRole;
import com.alzheimer.session_service.entities.SessionParticipant;
import com.alzheimer.session_service.entities.SessionStatus;
import com.alzheimer.session_service.entities.SessionType;
import com.alzheimer.session_service.entities.SessionVisibility;
import com.alzheimer.session_service.entities.VirtualSession;
import com.alzheimer.session_service.repositories.VirtualSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VirtualSessionService {

    private final VirtualSessionRepository repository;

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) { super(message); }
    }

    public VirtualSession create(CreateSessionRequest req, String requesterRole) {
        return createReservation(req, requesterRole);
    }

    public VirtualSession update(Long id, UpdateSessionRequest req, String requesterUserId, String requesterRole) {
        VirtualSession session = getById(id);
        ensureCanManage(session, requesterUserId, requesterRole);

        if (req.getEndTime().isBefore(req.getStartTime()) || req.getEndTime().equals(req.getStartTime())) {
            throw new BadRequestException("endTime must be after startTime");
        }

        SessionType sessionType = req.getSessionType() != null ? req.getSessionType() : session.getSessionType();
        String normalizedMeetingUrl = req.getMeetingUrl() != null
                ? normalizeMeetingUrl(req.getMeetingUrl())
                : normalizeMeetingUrl(session.getMeetingUrl());
        MeetingMode meetingMode = req.getMeetingMode() != null
                ? req.getMeetingMode()
                : (session.getMeetingMode() != null
                ? session.getMeetingMode()
                : resolveMeetingMode(null, normalizedMeetingUrl));
        String locationAddress = req.getLocationAddress() != null
                ? normalizeLocationAddress(req.getLocationAddress())
                : normalizeLocationAddress(session.getLocationAddress());
        Double locationLatitude = req.getLocationLatitude() != null
                ? normalizeLatitude(req.getLocationLatitude())
                : session.getLocationLatitude();
        Double locationLongitude = req.getLocationLongitude() != null
                ? normalizeLongitude(req.getLocationLongitude())
                : session.getLocationLongitude();

        if (meetingMode == MeetingMode.IN_PERSON) {
            normalizedMeetingUrl = null;
            validateLocationCoordinates(locationLatitude, locationLongitude);
        } else {
            locationAddress = null;
            locationLatitude = null;
            locationLongitude = null;
        }

        session.setTitle(req.getTitle());
        session.setDescription(req.getDescription());
        session.setStartTime(req.getStartTime());
        session.setEndTime(req.getEndTime());
        session.setMeetingUrl(normalizedMeetingUrl);
        session.setStatus(req.getStatus());
        session.setVisibility(req.getVisibility());
        session.setSessionType(sessionType);
        session.setMeetingMode(meetingMode);
        session.setLocationAddress(locationAddress);
        session.setLocationLatitude(locationLatitude);
        session.setLocationLongitude(locationLongitude);
        session.setUpdatedAt(Instant.now());

        return repository.save(session);
    }

    public void delete(Long id, String requesterUserId, String requesterRole) {
        VirtualSession session = getById(id);
        ensureCanManage(session, requesterUserId, requesterRole);
        repository.deleteById(id);
    }

    public VirtualSession getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Session not found: " + id));
    }

    public VirtualSession getById(Long id, String requesterUserId, String requesterRole) {
        VirtualSession session = getById(id);
        ensureCanRead(session, requesterUserId, requesterRole);
        return session;
    }

    public List<VirtualSession> list(Instant from, Instant to, SessionStatus status, String requesterUserId, String requesterRole) {
        List<VirtualSession> sessions;
        if (from != null && to != null) {
            if (status != null) {
                sessions = repository.findByStatusAndStartTimeBetween(status, from, to);
            } else {
                sessions = repository.findByStartTimeBetween(from, to);
            }
        } else if (status != null) {
            sessions = repository.findByStatus(status);
        } else if (from != null) {
            sessions = repository.findByStartTimeGreaterThanEqual(from);
        } else if (to != null) {
            sessions = repository.findByStartTimeLessThanEqual(to);
        } else {
            sessions = repository.findAll();
        }
        return sessions.stream()
                .filter(s -> canRead(s, requesterUserId, requesterRole))
                .toList();
    }

    public VirtualSession addParticipant(
            Long sessionId,
            AddParticipantRequest req,
            String requesterUserId,
            String requesterRole
    ) {
        String participantUserId = normalizeParticipantUserId(req.getUserId());
        VirtualSession session = getById(sessionId);
        ensureCanManage(session, requesterUserId, requesterRole);

        boolean exists = session.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(participantUserId));
        if (exists) {
            throw new BadRequestException("Participant already exists in this session");
        }

        session.getParticipants().add(SessionParticipant.builder()
                .userId(participantUserId)
                .role(req.getRole())
                .joinStatus(req.getJoinStatus())
                .joinedAt(null)
                .isFavorite(false)
                .reminderMinutesBefore(null)
                .build());

        session.setUpdatedAt(Instant.now());
        return repository.save(session);
    }

    public VirtualSession updateParticipantStatus(
            Long sessionId,
            UpdateParticipantStatusRequest req,
            String requesterUserId,
            String requesterRole
    ) {
        VirtualSession session = getById(sessionId);
        ensureCanRead(session, requesterUserId, requesterRole);
        SessionParticipant participant = findParticipant(session, requesterUserId);

        participant.setJoinStatus(req.getJoinStatus());
        if (req.isSetJoinedNow()) {
            participant.setJoinedAt(Instant.now());
        }

        session.setUpdatedAt(Instant.now());
        return repository.save(session);
    }

    public VirtualSession updateParticipantPrefs(
            Long sessionId,
            UpdateParticipantPrefsRequest req,
            String requesterUserId,
            String requesterRole
    ) {
        VirtualSession session = getById(sessionId);
        ensureCanRead(session, requesterUserId, requesterRole);
        SessionParticipant participant = findOrCreateParticipant(session, requesterUserId);

        if (req.getIsFavorite() != null) {
            participant.setFavorite(req.getIsFavorite());
        }
        participant.setReminderMinutesBefore(req.getReminderMinutesBefore());

        session.setUpdatedAt(Instant.now());
        return repository.save(session);
    }

    public VirtualSession setFavorite(Long sessionId, boolean favorite, String requesterUserId, String requesterRole) {
        UpdateParticipantPrefsRequest req = UpdateParticipantPrefsRequest.builder()
                .isFavorite(favorite)
                .build();
        return updateParticipantPrefs(sessionId, req, requesterUserId, requesterRole);
    }


    public List<SessionParticipant> listParticipants(Long sessionId, String requesterUserId, String requesterRole) {
        VirtualSession session = getById(sessionId);
        ensureCanRead(session, requesterUserId, requesterRole);
        return session.getParticipants();
    }

    public List<VirtualSession> listUserFavorites(String requesterUserId, String requesterRole) {
        return repository.findByParticipantUserId(requesterUserId).stream()
                .filter(session -> canRead(session, requesterUserId, requesterRole))
                .filter(session -> session.getParticipants().stream()
                        .anyMatch(p -> p.getUserId().equals(requesterUserId) && p.isFavorite()))
                .toList();
    }

    public List<VirtualSession> listUserReminders(Instant from, Instant to, String requesterUserId, String requesterRole) {
        return repository.findByParticipantUserId(requesterUserId).stream()
                .filter(session -> canRead(session, requesterUserId, requesterRole))
                .filter(session -> {
                    if (from != null && session.getStartTime().isBefore(from)) {
                        return false;
                    }
                    if (to != null && session.getStartTime().isAfter(to)) {
                        return false;
                    }

                    return session.getParticipants().stream().anyMatch(p ->
                            p.getUserId().equals(requesterUserId) && p.getReminderMinutesBefore() != null
                    );
                })
                .toList();
    }

    private SessionParticipant findOrCreateParticipant(VirtualSession session, String userId) {
        return session.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElseGet(() -> {
                    SessionParticipant created = SessionParticipant.builder()
                            .userId(userId)
                            .role(ParticipantRole.PARTICIPANT)
                            .joinStatus(JoinStatus.INVITED)
                            .joinedAt(null)
                            .isFavorite(false)
                            .reminderMinutesBefore(null)
                            .build();
                    session.getParticipants().add(created);
                    return created;
                });
    }

    private SessionParticipant findParticipant(VirtualSession session, String userId) {
        return session.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Participant not found for userId=" + userId));
    }

    private String normalizeParticipantUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("userId is required");
        }
        return userId.trim();
    }

    public VirtualSession createReservation(CreateSessionRequest req, String requesterRole) {
        if (req.getEndTime().isBefore(req.getStartTime()) || req.getEndTime().equals(req.getStartTime())) {
            throw new BadRequestException("endTime must be after startTime");
        }

        SessionType sessionType = req.getSessionType() != null ? req.getSessionType() : SessionType.PRIVATE;
        SessionVisibility visibility = resolveVisibility(req.getVisibility(), sessionType);
        MeetingMode meetingMode = resolveMeetingMode(req.getMeetingMode(), req.getMeetingUrl());
        String meetingUrl = normalizeMeetingUrl(req.getMeetingUrl());
        String locationAddress = normalizeLocationAddress(req.getLocationAddress());
        Double locationLatitude = normalizeLatitude(req.getLocationLatitude());
        Double locationLongitude = normalizeLongitude(req.getLocationLongitude());
        String createdBy = normalizeCreatedBy(req.getCreatedBy());
        SessionStatus status = resolveStatus(requesterRole, req.getStatus());

        if (meetingMode == MeetingMode.IN_PERSON) {
            meetingUrl = null;
            validateLocationCoordinates(locationLatitude, locationLongitude);
        } else {
            locationAddress = null;
            locationLatitude = null;
            locationLongitude = null;
        }

        VirtualSession session = VirtualSession.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .meetingUrl(meetingUrl)
                .createdBy(createdBy)
                .status(status)
                .visibility(visibility)
                .sessionType(sessionType)
                .meetingMode(meetingMode)
                .locationAddress(locationAddress)
                .locationLatitude(locationLatitude)
                .locationLongitude(locationLongitude)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return repository.save(session);
    }

    public VirtualSession respondToReservation(Long sessionId, boolean accept, String requesterUserId, String requesterRole) {
        VirtualSession session = getById(sessionId);
        ensureCanRead(session, requesterUserId, requesterRole);

        if (accept) {
            session.setStatus(SessionStatus.SCHEDULED);
        } else {
            session.setStatus(SessionStatus.CANCELLED);
        }

        return repository.save(session);
    }

    private SessionVisibility resolveVisibility(SessionVisibility requestedVisibility, SessionType sessionType) {
        if (requestedVisibility != null) {
            return requestedVisibility;
        }
        return sessionType == SessionType.GROUP ? SessionVisibility.PUBLIC : SessionVisibility.PRIVATE;
    }

    private MeetingMode resolveMeetingMode(MeetingMode requestedMode, String meetingUrl) {
        if (requestedMode != null) {
            return requestedMode;
        }
        return normalizeMeetingUrl(meetingUrl) != null ? MeetingMode.ONLINE : MeetingMode.IN_PERSON;
    }

    private SessionStatus resolveStatus(String requesterRole, SessionStatus requestedStatus) {
        if (isAdmin(requesterRole) && requestedStatus != null) {
            return requestedStatus;
        }
        return SessionStatus.DRAFT;
    }

    private String normalizeCreatedBy(String createdBy) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new BadRequestException("createdBy is required");
        }
        return createdBy.trim();
    }

    private String normalizeMeetingUrl(String meetingUrl) {
        if (meetingUrl == null || meetingUrl.trim().isEmpty()) {
            return null;
        }
        return meetingUrl.trim();
    }

    private String normalizeLocationAddress(String locationAddress) {
        if (locationAddress == null || locationAddress.trim().isEmpty()) {
            return null;
        }
        return locationAddress.trim();
    }

    private Double normalizeLatitude(Double latitude) {
        if (latitude == null) {
            return null;
        }
        if (latitude < -90 || latitude > 90) {
            throw new BadRequestException("locationLatitude must be between -90 and 90");
        }
        return latitude;
    }

    private Double normalizeLongitude(Double longitude) {
        if (longitude == null) {
            return null;
        }
        if (longitude < -180 || longitude > 180) {
            throw new BadRequestException("locationLongitude must be between -180 and 180");
        }
        return longitude;
    }

    private void validateLocationCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new BadRequestException("locationLatitude and locationLongitude are required for in-person sessions");
        }
    }

    private boolean isAdmin(String requesterRole) {
        return requesterRole != null && "ADMIN".equalsIgnoreCase(requesterRole);
    }

    private boolean canRead(VirtualSession session, String requesterUserId, String requesterRole) {
        if (isAdmin(requesterRole)) {
            return true;
        }
        if (requesterUserId != null && requesterUserId.equals(session.getCreatedBy())) {
            return true;
        }
        if (session.getVisibility() == SessionVisibility.PUBLIC) {
            return true;
        }
        return session.getParticipants().stream().anyMatch(p -> requesterUserId.equals(p.getUserId()));
    }

    private void ensureCanRead(VirtualSession session, String requesterUserId, String requesterRole) {
        if (!canRead(session, requesterUserId, requesterRole)) {
            throw new VideoSessionService.UnauthorizedException("Acces refuse a cette session.");
        }
    }

    private void ensureCanManage(VirtualSession session, String requesterUserId, String requesterRole) {
        if (isAdmin(requesterRole) || requesterUserId.equals(session.getCreatedBy())) {
            return;
        }
        boolean canManage = session.getParticipants().stream()
                .anyMatch(p -> requesterUserId.equals(p.getUserId())
                        && (p.getRole() == ParticipantRole.HOST || p.getRole() == ParticipantRole.ORGANIZER));
        if (!canManage) {
            throw new VideoSessionService.UnauthorizedException("Action reservee a l'organisateur.");
        }
    }
}
