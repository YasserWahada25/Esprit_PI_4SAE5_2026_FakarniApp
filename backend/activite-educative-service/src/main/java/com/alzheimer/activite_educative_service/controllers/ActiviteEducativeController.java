package com.alzheimer.activite_educative_service.controllers;

import com.alzheimer.activite_educative_service.dto.ActiviteEducativeRequest;
import com.alzheimer.activite_educative_service.dto.ActiviteEducativeResponse;
import com.alzheimer.activite_educative_service.dto.ActivitySubmitAnswerRequest;
import com.alzheimer.activite_educative_service.dto.GameSessionStartRequest;
import com.alzheimer.activite_educative_service.dto.GameSessionStartResponse;
import com.alzheimer.activite_educative_service.dto.SubmitAnswerResponse;
import com.alzheimer.activite_educative_service.dto.ImageCardPublicDto;
import com.alzheimer.activite_educative_service.services.ActiviteEducativeService;
import com.alzheimer.activite_educative_service.services.EducationalQuestionService;
import com.alzheimer.activite_educative_service.services.GameSessionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/activities", "/activities"})
public class ActiviteEducativeController {

    private static final Logger log = LoggerFactory.getLogger(ActiviteEducativeController.class);

    private final ActiviteEducativeService service;
    private final GameSessionService gameSessionService;
    private final EducationalQuestionService educationalQuestionService;

    public ActiviteEducativeController(
            ActiviteEducativeService service,
            GameSessionService gameSessionService,
            EducationalQuestionService educationalQuestionService
    ) {
        this.service = service;
        this.gameSessionService = gameSessionService;
        this.educationalQuestionService = educationalQuestionService;
    }

    /** Cartes image (MEMORY_MATCH), sans démarrer de session. */
    @GetMapping("/{activityId}/image-cards")
    public List<ImageCardPublicDto> listImageCards(@PathVariable Long activityId) {
        return educationalQuestionService.listImageCards(activityId);
    }

    /**
     * Démarrage de session de jeu (chemin canonique pour Gateway / front).
     * POST /api/activities/{activityId}/start
     */
    @PostMapping("/{activityId}/start")
    public GameSessionStartResponse startGameSession(
            @PathVariable Long activityId,
            @Valid @RequestBody GameSessionStartRequest request
    ) {
        log.info("Start game for activity {}", activityId);
        return gameSessionService.startGameSession(activityId, request);
    }

    /**
     * Variante GET (même effet que POST …/start avec corps {@code { "userId": … }}).
     */
    @GetMapping("/{activityId}/start")
    public GameSessionStartResponse startGameSessionGet(
            @PathVariable Long activityId,
            @RequestParam Long userId
    ) {
        GameSessionStartRequest req = new GameSessionStartRequest();
        req.setUserId(userId);
        return gameSessionService.startGameSession(activityId, req);
    }

    @PostMapping("/{activityId}/submit-answer")
    public SubmitAnswerResponse submitAnswerForActivity(
            @PathVariable Long activityId,
            @Valid @RequestBody ActivitySubmitAnswerRequest body
    ) {
        return gameSessionService.submitAnswerForActivity(activityId, body);
    }

    @PostMapping
    public ActiviteEducativeResponse createActivity(@Valid @RequestBody ActiviteEducativeRequest request) {
        return service.createActivity(request);
    }

    /**
     * Création avec miniature optionnelle (multipart : part {@code activity} = JSON,
     * part {@code thumbnail} = fichier image).
     */
    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActiviteEducativeResponse createActivityMultipart(
            @Valid @RequestPart("activity") ActiviteEducativeRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        return service.createActivity(request, thumbnail);
    }

    /**
     * Mise à jour avec miniature optionnelle (même schéma multipart que {@link #createActivityMultipart}).
     */
    @PutMapping(value = "/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActiviteEducativeResponse updateActivityMultipart(
            @PathVariable Long id,
            @Valid @RequestPart("activity") ActiviteEducativeRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        return service.updateActivity(id, request, thumbnail);
    }

    @GetMapping
    public List<ActiviteEducativeResponse> getAllActivities(
            @RequestParam(required = false) Long userId
    ) {
        List<ActiviteEducativeResponse> list = service.getAllActivities(userId);
        log.info("GET /api/activities → {} activities (userId={})", list.size(), userId);
        return list;
    }

    @GetMapping("/{id}")
    public ActiviteEducativeResponse getActivityById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId
    ) {
        return service.getActivityById(id, userId);
    }

    @PutMapping("/{id}")
    public ActiviteEducativeResponse updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActiviteEducativeRequest request
    ) {
        return service.updateActivity(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteActivity(@PathVariable Long id) {
        service.deleteActivity(id);
    }
}

