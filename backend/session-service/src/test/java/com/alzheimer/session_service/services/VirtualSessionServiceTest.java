package com.alzheimer.session_service.services;

import com.alzheimer.session_service.dto.CreateSessionRequest;
import com.alzheimer.session_service.dto.UpdateParticipantPrefsRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VirtualSessionServiceTest {

    @Mock
    private VirtualSessionRepository repository;

    @InjectMocks
    private VirtualSessionService service;

    @Test
    void updateParticipantPrefs_createsParticipantWhenMissing() {
        VirtualSession session = buildSession(1L);
        UpdateParticipantPrefsRequest request = UpdateParticipantPrefsRequest.builder()
                .isFavorite(true)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(session));
        when(repository.save(any(VirtualSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VirtualSession updated = service.updateParticipantPrefs(1L, request, "admin", "ADMIN");

        assertNotNull(updated);
        assertEquals(1, updated.getParticipants().size());
        SessionParticipant participant = updated.getParticipants().get(0);
        assertEquals("admin", participant.getUserId());
        assertEquals(ParticipantRole.PARTICIPANT, participant.getRole());
        assertEquals(JoinStatus.INVITED, participant.getJoinStatus());
        assertTrue(participant.isFavorite());
        verify(repository).save(session);
    }

    @Test
    void setFavorite_updatesExistingParticipantFavoriteState() {
        VirtualSession session = buildSession(2L);
        session.getParticipants().add(
                SessionParticipant.builder()
                        .userId("admin")
                        .role(ParticipantRole.PARTICIPANT)
                        .joinStatus(JoinStatus.CONFIRMED)
                        .isFavorite(true)
                        .build()
        );

        when(repository.findById(2L)).thenReturn(Optional.of(session));
        when(repository.save(any(VirtualSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VirtualSession updated = service.setFavorite(2L, false, "admin", "ADMIN");

        assertNotNull(updated);
        assertFalse(updated.getParticipants().get(0).isFavorite());
        verify(repository).save(session);
    }

    @Test
    void listUserFavorites_returnsOnlyFavoriteSessions() {
        VirtualSession favoriteSession = buildSession(10L);
        favoriteSession.getParticipants().add(
                SessionParticipant.builder()
                        .userId("admin")
                        .role(ParticipantRole.PARTICIPANT)
                        .joinStatus(JoinStatus.CONFIRMED)
                        .isFavorite(true)
                        .build()
        );

        VirtualSession nonFavoriteSession = buildSession(11L);
        nonFavoriteSession.getParticipants().add(
                SessionParticipant.builder()
                        .userId("admin")
                        .role(ParticipantRole.PARTICIPANT)
                        .joinStatus(JoinStatus.CONFIRMED)
                        .isFavorite(false)
                        .build()
        );

        when(repository.findByParticipantUserId("admin"))
                .thenReturn(List.of(favoriteSession, nonFavoriteSession));

        List<VirtualSession> favorites = service.listUserFavorites("admin", "ADMIN");

        assertEquals(1, favorites.size());
        assertEquals(10L, favorites.get(0).getId());
    }

    @Test
    void list_withOnlyStatus_filtersByStatus() {
        VirtualSession pending = buildSession(20L);
        pending.setStatus(SessionStatus.DRAFT);

        when(repository.findByStatus(SessionStatus.DRAFT)).thenReturn(List.of(pending));

        List<VirtualSession> result = service.list(null, null, SessionStatus.DRAFT, "admin", "ADMIN");

        assertEquals(1, result.size());
        assertEquals(SessionStatus.DRAFT, result.get(0).getStatus());
        verify(repository).findByStatus(SessionStatus.DRAFT);
    }

    @Test
    void createReservation_onlineWithoutMeetingUrl_keepsCreationFlow() {
        CreateSessionRequest request = CreateSessionRequest.builder()
                .title("Session video")
                .description("Session sans url saisie manuellement")
                .startTime(Instant.parse("2026-03-12T08:00:00Z"))
                .endTime(Instant.parse("2026-03-12T09:00:00Z"))
                .createdBy("admin")
                .status(SessionStatus.SCHEDULED)
                .visibility(SessionVisibility.PUBLIC)
                .sessionType(SessionType.GROUP)
                .meetingMode(MeetingMode.ONLINE)
                .build();

        when(repository.save(any(VirtualSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VirtualSession created = service.createReservation(request, "ADMIN");

        assertNotNull(created);
        assertEquals(MeetingMode.ONLINE, created.getMeetingMode());
        assertNull(created.getMeetingUrl());
    }

    @Test
    void update_withoutMeetingUrl_preservesExistingMeetingUrl() {
        VirtualSession existing = buildSession(30L);
        existing.setMeetingUrl("https://meet.example.com/existing-room");

        UpdateSessionRequest request = UpdateSessionRequest.builder()
                .title("Session test mise a jour")
                .description("description mise a jour")
                .startTime(Instant.parse("2026-02-21T11:00:00Z"))
                .endTime(Instant.parse("2026-02-21T12:00:00Z"))
                .status(SessionStatus.SCHEDULED)
                .visibility(SessionVisibility.PUBLIC)
                .build();

        when(repository.findById(30L)).thenReturn(Optional.of(existing));
        when(repository.save(any(VirtualSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VirtualSession updated = service.update(30L, request, "admin", "ADMIN");

        assertEquals("https://meet.example.com/existing-room", updated.getMeetingUrl());
    }

    @Test
    void createReservation_byPatient_isPrivateDraftEvenForGroup() {
        CreateSessionRequest request = CreateSessionRequest.builder()
                .title("Atelier memoire")
                .description("Demande de seance de groupe")
                .startTime(Instant.parse("2026-03-12T08:00:00Z"))
                .endTime(Instant.parse("2026-03-12T09:00:00Z"))
                .createdBy("patient-1")
                .status(SessionStatus.SCHEDULED)
                .visibility(SessionVisibility.PUBLIC)
                .sessionType(SessionType.GROUP)
                .meetingMode(MeetingMode.ONLINE)
                .build();
        when(repository.save(any(VirtualSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VirtualSession created = service.createReservation(request, "PATIENT_PROFILE");

        assertEquals(SessionVisibility.PRIVATE, created.getVisibility());
        assertEquals(SessionStatus.DRAFT, created.getStatus());
        assertEquals(SessionType.GROUP, created.getSessionType());
    }

    @Test
    void list_patientSeesOwnRequestsAndPublicSessionsOnly() {
        VirtualSession mine = buildSession(1L);
        mine.setCreatedBy("patient-1");
        mine.setVisibility(SessionVisibility.PRIVATE);
        VirtualSession othersRequest = buildSession(2L);
        othersRequest.setCreatedBy("patient-2");
        othersRequest.setVisibility(SessionVisibility.PRIVATE);
        VirtualSession invited = buildSession(3L);
        invited.setCreatedBy("patient-2");
        invited.setVisibility(SessionVisibility.PRIVATE);
        invited.getParticipants().add(SessionParticipant.builder()
                .userId("doctor-1").role(ParticipantRole.ORGANIZER).joinStatus(JoinStatus.CONFIRMED).build());
        VirtualSession adminPublic = buildSession(4L);
        when(repository.findAll()).thenReturn(List.of(mine, othersRequest, invited, adminPublic));

        assertEquals(List.of(1L, 4L),
                service.list(null, null, null, "patient-1", "PATIENT_PROFILE").stream().map(VirtualSession::getId).toList());
        assertEquals(List.of(3L, 4L),
                service.list(null, null, null, "doctor-1", "DOCTOR_PROFILE").stream().map(VirtualSession::getId).toList());
        assertEquals(4, service.list(null, null, null, "admin", "ADMIN").size());
    }

    @Test
    void update_byPatient_keepsStatusAndVisibility() {
        VirtualSession existing = buildSession(31L);
        existing.setCreatedBy("patient-1");
        existing.setStatus(SessionStatus.DRAFT);
        existing.setVisibility(SessionVisibility.PRIVATE);
        existing.setMeetingMode(MeetingMode.ONLINE);
        UpdateSessionRequest request = UpdateSessionRequest.builder()
                .title("Nouveau titre")
                .description("description")
                .startTime(Instant.parse("2026-02-21T11:00:00Z"))
                .endTime(Instant.parse("2026-02-21T12:00:00Z"))
                .status(SessionStatus.SCHEDULED)
                .visibility(SessionVisibility.PUBLIC)
                .build();
        when(repository.findById(31L)).thenReturn(Optional.of(existing));
        when(repository.save(any(VirtualSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VirtualSession updated = service.update(31L, request, "patient-1", "PATIENT_PROFILE");

        assertEquals("Nouveau titre", updated.getTitle());
        assertEquals(SessionStatus.DRAFT, updated.getStatus());
        assertEquals(SessionVisibility.PRIVATE, updated.getVisibility());
    }

    @Test
    void respondToReservation_onlyAdminCanAccept() {
        assertThrows(VideoSessionService.UnauthorizedException.class,
                () -> service.respondToReservation(5L, true, "patient-1", "PATIENT_PROFILE"));
        verify(repository, never()).save(any(VirtualSession.class));

        VirtualSession request = buildSession(5L);
        request.setStatus(SessionStatus.DRAFT);
        when(repository.findById(5L)).thenReturn(Optional.of(request));
        when(repository.save(any(VirtualSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(SessionStatus.SCHEDULED, service.respondToReservation(5L, true, "admin", "ADMIN").getStatus());
    }

    private VirtualSession buildSession(Long id) {
        return VirtualSession.builder()
                .id(id)
                .title("Session test")
                .description("description")
                .startTime(Instant.parse("2026-02-21T09:00:00Z"))
                .endTime(Instant.parse("2026-02-21T10:00:00Z"))
                .createdBy("admin")
                .status(SessionStatus.SCHEDULED)
                .visibility(SessionVisibility.PUBLIC)
                .sessionType(SessionType.GROUP)
                .build();
    }
}
