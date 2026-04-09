package com.alzheimer.session_service.controllers;

import com.alzheimer.session_service.dto.AddParticipantRequest;
import com.alzheimer.session_service.dto.CreateSessionRequest;
import com.alzheimer.session_service.dto.UpdateParticipantPrefsRequest;
import com.alzheimer.session_service.dto.UpdateParticipantStatusRequest;
import com.alzheimer.session_service.dto.UpdateSessionRequest;
import com.alzheimer.session_service.entities.SessionParticipant;
import com.alzheimer.session_service.entities.SessionStatus;
import com.alzheimer.session_service.entities.VirtualSession;
import com.alzheimer.session_service.security.AuthenticatedUser;
import com.alzheimer.session_service.services.VirtualSessionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/session")
public class VirtualSessionController {

    private final VirtualSessionService service;

    public VirtualSessionController(VirtualSessionService service) {
        this.service = service;
    }

    @PostMapping("/sessions")
    public VirtualSession createSession(@Valid @RequestBody CreateSessionRequest req, @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser authUser = AuthenticatedUser.fromJwt(jwt);
        req.setCreatedBy(authUser.userId());
        return service.createReservation(req, authUser.role());
    }

    @PutMapping("/sessions/{id}")
    public VirtualSession update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSessionRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedUser authUser = AuthenticatedUser.fromJwt(jwt);
        return service.update(id, req, authUser.userId(), authUser.role());
    }

    @DeleteMapping("/sessions/{id}")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser authUser = AuthenticatedUser.fromJwt(jwt);
        service.delete(id, authUser.userId(), authUser.role());
    }

    @GetMapping("/sessions/{id}")
    public VirtualSession get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser authUser = AuthenticatedUser.fromJwt(jwt);
        return service.getById(id, authUser.userId(), authUser.role());
    }

    @GetMapping("/sessions")
    public List<VirtualSession> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) SessionStatus status,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedUser authUser = AuthenticatedUser.fromJwt(jwt);
        return service.list(from, to, status, authUser.userId(), authUser.role());
    }

    @PostMapping("/sessions/{id}/participants")
    public VirtualSession addParticipant(
            @PathVariable Long id,
            @Valid @RequestBody AddParticipantRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedUser authUser = AuthenticatedUser.fromJwt(jwt);
        return service.addParticipant(id, req, authUser.userId(), authUser.role());
    }

    @PatchMapping("/sessions/{id}/participants/me")
    public VirtualSession updateMyParticipantStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateParticipantStatusRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedUser authUser = AuthenticatedUser.fromJwt(jwt);
        return service.updateParticipantStatus(id, req, authUser.userId(), authUser.role());
    }

    @PatchMapping("/sessions/{id}/participants/me/prefs")
    public VirtualSession updateMyParticipantPrefs(
            @PathVariable Long id,
            @Valid @RequestBody UpdateParticipantPrefsRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedUser authUser = AuthenticatedUser.fromJwt(jwt);
        return service.updateParticipantPrefs(id, req, authUser.userId(), authUser.role());
    }

    @GetMapping("/sessions/{id}/participants")
    public List<SessionParticipant> listParticipants(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser authUser = AuthenticatedUser.fromJwt(jwt);
        return service.listParticipants(id, authUser.userId(), authUser.role());
    }

    @GetMapping("/me/favorites")
    public List<VirtualSession> favorites(@AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser authUser = AuthenticatedUser.fromJwt(jwt);
        return service.listUserFavorites(authUser.userId(), authUser.role());
    }

    @GetMapping("/me/reminders")
    public List<VirtualSession> reminders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedUser authUser = AuthenticatedUser.fromJwt(jwt);
        return service.listUserReminders(from, to, authUser.userId(), authUser.role());
    }

    @PatchMapping("/sessions/{id}/response")
    public VirtualSession respondToSession(
            @PathVariable Long id,
            @RequestParam boolean accept,
            @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            AuthenticatedUser authUser = AuthenticatedUser.fromJwt(jwt);
            return service.respondToReservation(id, accept, authUser.userId(), authUser.role());
        } catch (VirtualSessionService.BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur serveur interne", e);
        }
    }
}
