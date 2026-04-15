package com.alzheimer.activite_educative_service.services;

import com.alzheimer.activite_educative_service.dto.ActiviteEducativeRequest;
import com.alzheimer.activite_educative_service.entities.ActiviteEducative;
import com.alzheimer.activite_educative_service.entities.ActivityType;
import com.alzheimer.activite_educative_service.exceptions.BusinessRuleException;

final class ActivityBusinessRules {

    private ActivityBusinessRules() {
    }

    static void validatePersistRequest(ActiviteEducativeRequest request) {
        if (request.getType() == null) {
            throw new BusinessRuleException("type is required");
        }
        if (request.getType() == ActivityType.GAME && request.getGameType() == null) {
            throw new BusinessRuleException("gameType is required when type is GAME");
        }
        if (request.getType() == ActivityType.GAME && request.getScoreThreshold() == null) {
            throw new BusinessRuleException("scoreThreshold is required when type is GAME (0–100, minimum % to pass)");
        }
        if (request.getType() == ActivityType.CONTENT && request.getGameType() != null) {
            throw new BusinessRuleException("gameType must be null when type is CONTENT");
        }
        if (request.getType() == ActivityType.CONTENT && request.getScoreThreshold() != null) {
            throw new BusinessRuleException("scoreThreshold must be null when type is CONTENT");
        }
        if (request.getType() == ActivityType.VIDEO && request.getGameType() != null) {
            throw new BusinessRuleException("gameType must be null when type is VIDEO");
        }
        if (request.getType() == ActivityType.VIDEO && request.getScoreThreshold() != null) {
            throw new BusinessRuleException("scoreThreshold must be null when type is VIDEO");
        }
    }

    static void validateEntity(ActiviteEducative entity) {
        if (entity.getType() == ActivityType.GAME && entity.getGameType() == null) {
            throw new BusinessRuleException("gameType is required when type is GAME");
        }
        if (entity.getType() == ActivityType.GAME && entity.getScoreThreshold() == null) {
            throw new BusinessRuleException("scoreThreshold is required when type is GAME");
        }
        if (entity.getType() == ActivityType.CONTENT && entity.getGameType() != null) {
            throw new BusinessRuleException("gameType must be null when type is CONTENT");
        }
        if (entity.getType() == ActivityType.CONTENT && entity.getScoreThreshold() != null) {
            throw new BusinessRuleException("scoreThreshold must be null when type is CONTENT");
        }
        if (entity.getType() == ActivityType.VIDEO && entity.getGameType() != null) {
            throw new BusinessRuleException("gameType must be null when type is VIDEO");
        }
        if (entity.getType() == ActivityType.VIDEO && entity.getScoreThreshold() != null) {
            throw new BusinessRuleException("scoreThreshold must be null when type is VIDEO");
        }
        // QUIZ legacy : gameType souvent null en base — ne pas appliquer la règle GAME stricte
    }
}
