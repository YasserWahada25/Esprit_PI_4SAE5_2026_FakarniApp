package com.alzheimer.activite_educative_service.controllers;

import com.alzheimer.activite_educative_service.dto.GameSessionResultResponse;
import com.alzheimer.activite_educative_service.dto.MemoryMoveRequest;
import com.alzheimer.activite_educative_service.dto.MemoryMoveResponse;
import com.alzheimer.activite_educative_service.services.GameSessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
}
