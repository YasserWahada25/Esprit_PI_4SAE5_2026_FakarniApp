package com.alzheimer.activite_educative_service.services;

import com.alzheimer.activite_educative_service.dto.ActiviteEducativeRequest;
import com.alzheimer.activite_educative_service.dto.ActiviteEducativeResponse;
import com.alzheimer.activite_educative_service.entities.ActiviteEducative;
import com.alzheimer.activite_educative_service.entities.ActivityStatus;
import com.alzheimer.activite_educative_service.entities.ActivityType;
import com.alzheimer.activite_educative_service.entities.SessionStatus;
import com.alzheimer.activite_educative_service.exceptions.ResourceNotFoundException;
import com.alzheimer.activite_educative_service.repositories.ActiviteEducativeRepository;
import com.alzheimer.activite_educative_service.repositories.GameSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActiviteEducativeService {

    private static final Logger log = LoggerFactory.getLogger(ActiviteEducativeService.class);

    private final ActiviteEducativeRepository activiteEducativeRepository;
    private final GameSessionRepository gameSessionRepository;
    private final MediaStorageService mediaStorageService;

    public ActiviteEducativeService(
            ActiviteEducativeRepository activiteEducativeRepository,
            GameSessionRepository gameSessionRepository,
            MediaStorageService mediaStorageService
    ) {
        this.activiteEducativeRepository = activiteEducativeRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.mediaStorageService = mediaStorageService;
    }

    @Transactional
    public ActiviteEducativeResponse createActivity(ActiviteEducativeRequest request) {
        return createActivity(request, null);
    }

    @Transactional
    public ActiviteEducativeResponse createActivity(ActiviteEducativeRequest request, MultipartFile thumbnail) {
        ActivityBusinessRules.validatePersistRequest(request);
        if (thumbnail != null && !thumbnail.isEmpty()) {
            request.setThumbnailUrl(mediaStorageService.storeActivityThumbnail(thumbnail));
        }
        ActiviteEducative entity = new ActiviteEducative();
        applyRequest(entity, request);
        if (entity.getStatus() == null) {
            entity.setStatus(ActivityStatus.ACTIVE);
        }
        ActivityBusinessRules.validateEntity(entity);
        ActiviteEducative saved = activiteEducativeRepository.save(entity);
        return new ActiviteEducativeResponse(saved);
    }

    @Transactional
    public ActiviteEducativeResponse updateActivity(Long id, ActiviteEducativeRequest request) {
        return updateActivity(id, request, null);
    }

    @Transactional
    public ActiviteEducativeResponse updateActivity(Long id, ActiviteEducativeRequest request, MultipartFile thumbnail) {
        ActivityBusinessRules.validatePersistRequest(request);
        ActiviteEducative entity = activiteEducativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + id));
        if (thumbnail != null && !thumbnail.isEmpty()) {
            request.setThumbnailUrl(mediaStorageService.storeActivityThumbnail(thumbnail));
        } else if (request.getThumbnailUrl() == null || request.getThumbnailUrl().isBlank()) {
            request.setThumbnailUrl(entity.getThumbnailUrl());
        }
        applyRequest(entity, request);
        ActivityBusinessRules.validateEntity(entity);
        ActiviteEducative updated = activiteEducativeRepository.save(entity);
        return new ActiviteEducativeResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<ActiviteEducativeResponse> getAllActivities(Long userId) {
        List<ActiviteEducative> entities = activiteEducativeRepository.findAll();
        log.info("Retrieved {} educational activities from repository (userId filter for scores: {})",
                entities.size(), userId);
        List<ActiviteEducativeResponse> list = entities.stream()
                .map(a -> toResponse(a, userId))
                .collect(Collectors.toList());
        log.debug("Mapped {} activity DTOs for API response", list.size());
        return list;
    }

    @Transactional(readOnly = true)
    public ActiviteEducativeResponse getActivityById(Long id, Long userId) {
        ActiviteEducative entity = activiteEducativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + id));
        return toResponse(entity, userId);
    }

    @Transactional
    public void deleteActivity(Long id) {
        if (!activiteEducativeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Activity not found: " + id);
        }
        activiteEducativeRepository.deleteById(id);
    }

    private void applyRequest(ActiviteEducative entity, ActiviteEducativeRequest request) {
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setType(request.getType());
        entity.setGameType(request.getGameType());
        entity.setIconKey(request.getIconKey());
        entity.setScoreThreshold(request.getScoreThreshold());
        entity.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
    }

    private ActiviteEducativeResponse toResponse(ActiviteEducative entity, Long userId) {
        ActiviteEducativeResponse r = new ActiviteEducativeResponse(entity);
        if (userId != null && isGameLikeForScores(entity.getType())) {
            gameSessionRepository
                    .findFirstByUserIdAndActivity_IdAndStatusInOrderByFinishedAtDesc(
                            userId,
                            entity.getId(),
                            EnumSet.of(SessionStatus.SUCCESS, SessionStatus.FAILURE, SessionStatus.COMPLETED))
                    .ifPresent(s -> r.setLatestScorePercent(s.getScorePercent()));
        }
        return r;
    }

    /** GAME et QUIZ (legacy) peuvent avoir des sessions avec score. */
    private static boolean isGameLikeForScores(ActivityType type) {
        return type == ActivityType.GAME || type == ActivityType.QUIZ;
    }
}
