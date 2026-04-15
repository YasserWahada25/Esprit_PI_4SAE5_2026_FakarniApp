package com.alzheimer.activite_educative_service.controllers;

import com.alzheimer.activite_educative_service.dto.GameSessionStartRequest;
import com.alzheimer.activite_educative_service.dto.GameSessionStartResponse;
import com.alzheimer.activite_educative_service.services.GameSessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Variante REST : démarrage de session sous la ressource activité.
 */
@RestController
@RequestMapping({
        "/api/activities/{activityId}/game-sessions",
        "/activities/{activityId}/game-sessions"
})
public class ActivityGameSessionController {

    private final GameSessionService gameSessionService;

    public ActivityGameSessionController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @PostMapping
    public GameSessionStartResponse start(
            @PathVariable Long activityId,
            @Valid @RequestBody GameSessionStartRequest request
    ) {
        return gameSessionService.startGameSession(activityId, request);
    }
}
