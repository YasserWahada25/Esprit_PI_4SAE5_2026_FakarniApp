package com.alzheimer.activite_educative_service.controllers;

import com.alzheimer.activite_educative_service.dto.GameSessionHistoryItemResponse;
import com.alzheimer.activite_educative_service.dto.GameSessionResultResponse;
import com.alzheimer.activite_educative_service.dto.MemoryMoveRequest;
import com.alzheimer.activite_educative_service.dto.MemoryMoveResponse;
import com.alzheimer.activite_educative_service.services.GameSessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Chemins REST raccourcis {@code /api/game-sessions/...} (en plus de
 * {@code /api/activities/game-sessions/...}).
 */
@RestController
@RequestMapping({"/api/game-sessions", "/game-sessions"})
public class ApiGameSessionController {

    private final GameSessionService gameSessionService;

    public ApiGameSessionController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @PostMapping("/{sessionId}/move")
    public MemoryMoveResponse move(
            @PathVariable Long sessionId,
            @Valid @RequestBody MemoryMoveRequest request
    ) {
        return gameSessionService.submitMemoryMove(sessionId, request);
    }

    @GetMapping("/{sessionId}/result")
    public GameSessionResultResponse result(@PathVariable Long sessionId) {
        return gameSessionService.getSessionResult(sessionId);
    }

    @PostMapping("/{sessionId}/abandon")
    public GameSessionHistoryItemResponse abandon(@PathVariable Long sessionId) {
        return gameSessionService.abandonSession(sessionId);
    }

    /**
     * Agrégation suivi d’engagement : sessions récentes avec {@code patientId} renseigné (max 500).
     * Placé ici pour éviter la collision avec {@code GET /{sessionId}/result} sur le même préfixe.
     */
    @GetMapping("/engagement-sessions")
    public List<GameSessionHistoryItemResponse> engagementSessions(
            @RequestParam(name = "limit", defaultValue = "500") int limit
    ) {
        return gameSessionService.listRecentForEngagement(limit);
    }
}
