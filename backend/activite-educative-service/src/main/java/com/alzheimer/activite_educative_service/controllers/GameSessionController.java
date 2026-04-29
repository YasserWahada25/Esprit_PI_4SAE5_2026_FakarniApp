package com.alzheimer.activite_educative_service.controllers;

import com.alzheimer.activite_educative_service.dto.*;
import com.alzheimer.activite_educative_service.services.GameSessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping({"/api/activities/game-sessions", "/activities/game-sessions"})
public class GameSessionController {

    private final GameSessionService gameSessionService;

    public GameSessionController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    /** Démarre une session pour une activité de type GAME. */
    @PostMapping("/start/{activityId}")
    public GameSessionStartResponse start(
            @PathVariable Long activityId,
            @Valid @RequestBody GameSessionStartRequest request
    ) {
        return gameSessionService.startGameSession(activityId, request);
    }

    @PostMapping("/{sessionId}/answers")
    public SubmitAnswerResponse submit(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitAnswerRequest request
    ) {
        return gameSessionService.submitAnswer(sessionId, request);
    }

    @PostMapping("/{sessionId}/finish")
    public GameSessionResultResponse finish(@PathVariable Long sessionId) {
        return gameSessionService.finishSession(sessionId);
    }

    @PostMapping("/{sessionId}/abandon")
    public GameSessionHistoryItemResponse abandon(@PathVariable Long sessionId) {
        return gameSessionService.abandonSession(sessionId);
    }

    /** Memory paires : même logique que {@code POST /api/game-sessions/{id}/move} (chemin compatible gateway). */
    @PostMapping("/{sessionId}/move")
    public MemoryMoveResponse memoryMove(
            @PathVariable Long sessionId,
            @Valid @RequestBody MemoryMoveRequest request
    ) {
        return gameSessionService.submitMemoryMove(sessionId, request);
    }

    @GetMapping("/{sessionId}")
    public GameSessionResultResponse getResult(@PathVariable Long sessionId) {
        return gameSessionService.getSessionResult(sessionId);
    }

    @GetMapping("/history")
    public List<GameSessionHistoryItemResponse> history(@RequestParam @NotBlank String patientId) {
        return gameSessionService.getHistory(patientId);
    }
}
