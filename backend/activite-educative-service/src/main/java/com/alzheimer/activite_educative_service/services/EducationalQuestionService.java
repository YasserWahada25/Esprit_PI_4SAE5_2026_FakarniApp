package com.alzheimer.activite_educative_service.services;

import com.alzheimer.activite_educative_service.dto.ImageCardPublicDto;
import com.alzheimer.activite_educative_service.dto.QuestionRequest;
import com.alzheimer.activite_educative_service.dto.QuestionResponse;
import com.alzheimer.activite_educative_service.entities.ActiviteEducative;
import com.alzheimer.activite_educative_service.entities.ActivityType;
import com.alzheimer.activite_educative_service.entities.EducationalQuestion;
import com.alzheimer.activite_educative_service.entities.GameType;
import com.alzheimer.activite_educative_service.exceptions.BusinessRuleException;
import com.alzheimer.activite_educative_service.exceptions.ResourceNotFoundException;
import com.alzheimer.activite_educative_service.repositories.ActiviteEducativeRepository;
import com.alzheimer.activite_educative_service.repositories.EducationalQuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EducationalQuestionService {

    private final ActiviteEducativeRepository activiteEducativeRepository;
    private final EducationalQuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final MediaStorageService mediaStorageService;

    public EducationalQuestionService(
            ActiviteEducativeRepository activiteEducativeRepository,
            EducationalQuestionRepository questionRepository,
            ObjectMapper objectMapper,
            MediaStorageService mediaStorageService
    ) {
        this.activiteEducativeRepository = activiteEducativeRepository;
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
        this.mediaStorageService = mediaStorageService;
    }

    /**
     * Cartes image pour activité MEMORY_MATCH (aperçu sans session).
     */
    @Transactional(readOnly = true)
    public List<ImageCardPublicDto> listImageCards(Long activityId) {
        ActiviteEducative a = activiteEducativeRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));
        if (a.getGameType() != GameType.MEMORY_MATCH && a.getGameType() != GameType.PUZZLE) {
            return List.of();
        }
        List<ImageCardPublicDto> out = new ArrayList<>();
        for (EducationalQuestion q : questionRepository.findByActivityIdOrderByOrderIndexAsc(activityId)) {
            ImageCardPublicDto d = new ImageCardPublicDto();
            d.setId(q.getId());
            d.setImageUrl(q.getImageUrl());
            d.setLabel(q.getPrompt());
            out.add(d);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listQuestions(Long activityId) {
        ActiviteEducative a = activiteEducativeRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));
        if (a.getType() != ActivityType.GAME && a.getType() != ActivityType.QUIZ) {
            return List.of();
        }
        return questionRepository.findByActivityIdOrderByOrderIndexAsc(activityId).stream()
                .map(q -> new QuestionResponse(q, objectMapper))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestion(Long activityId, Long questionId) {
        EducationalQuestion q = loadQuestion(activityId, questionId);
        return new QuestionResponse(q, objectMapper);
    }

    @Transactional
    public QuestionResponse createQuestion(Long activityId, QuestionRequest request) {
        return createQuestion(activityId, request, null);
    }

    @Transactional
    public QuestionResponse createQuestion(Long activityId, QuestionRequest request, MultipartFile imageFile) {
        ActiviteEducative activity = loadActivityForQuestions(activityId);
        if (imageFile != null && !imageFile.isEmpty()) {
            request.setImageUrl(mediaStorageService.storeQuestionImage(imageFile));
        }
        validateQuestionAgainstGameType(activity.getGameType(), request);
        if (activity.getGameType() != GameType.MEMORY_MATCH && activity.getGameType() != GameType.PUZZLE) {
            validateCorrectAnswerInOptions(request);
        }

        EducationalQuestion q = new EducationalQuestion();
        q.setActivity(activity);
        applyRequest(q, request);
        EducationalQuestion saved = questionRepository.save(q);
        return new QuestionResponse(saved, objectMapper);
    }

    @Transactional
    public QuestionResponse updateQuestion(Long activityId, Long questionId, QuestionRequest request) {
        return updateQuestion(activityId, questionId, request, null);
    }

    @Transactional
    public QuestionResponse updateQuestion(
            Long activityId,
            Long questionId,
            QuestionRequest request,
            MultipartFile imageFile
    ) {
        EducationalQuestion q = loadQuestion(activityId, questionId);
        if (imageFile != null && !imageFile.isEmpty()) {
            request.setImageUrl(mediaStorageService.storeQuestionImage(imageFile));
        } else if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
            request.setImageUrl(q.getImageUrl());
        }
        validateQuestionAgainstGameType(q.getActivity().getGameType(), request);
        if (q.getActivity().getGameType() != GameType.MEMORY_MATCH && q.getActivity().getGameType() != GameType.PUZZLE) {
            validateCorrectAnswerInOptions(request);
        }
        applyRequest(q, request);
        return new QuestionResponse(questionRepository.save(q), objectMapper);
    }

    @Transactional
    public void deleteQuestion(Long activityId, Long questionId) {
        EducationalQuestion q = loadQuestion(activityId, questionId);
        questionRepository.delete(q);
    }

    private ActiviteEducative loadActivityForQuestions(Long activityId) {
        ActiviteEducative activity = activiteEducativeRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));
        if (activity.getType() != ActivityType.GAME && activity.getType() != ActivityType.QUIZ) {
            throw new BusinessRuleException("Questions can only be attached to GAME or legacy QUIZ activities");
        }
        return activity;
    }

    private EducationalQuestion loadQuestion(Long activityId, Long questionId) {
        EducationalQuestion q = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + questionId));
        if (!q.getActivity().getId().equals(activityId)) {
            throw new ResourceNotFoundException("Question not found for this activity");
        }
        return q;
    }

    private void applyRequest(EducationalQuestion q, QuestionRequest request) {
        q.setOrderIndex(request.getOrderIndex());
        q.setPrompt(request.getPrompt());
        q.setImageUrl(request.getImageUrl());
        q.setCorrectAnswer(request.getCorrectAnswer().trim());
        try {
            List<String> opts = request.getOptions();
            if (opts == null || opts.isEmpty()) {
                q.setOptionsJson("[]");
            } else {
                q.setOptionsJson(objectMapper.writeValueAsString(opts));
            }
        } catch (JsonProcessingException e) {
            throw new BusinessRuleException("Invalid options payload");
        }
    }

    private void validateCorrectAnswerInOptions(QuestionRequest request) {
        String normalized = request.getCorrectAnswer().trim().toLowerCase();
        boolean ok = request.getOptions().stream()
                .anyMatch(o -> o != null && o.trim().toLowerCase().equals(normalized));
        if (!ok) {
            throw new BusinessRuleException("correctAnswer must match one of the options");
        }
    }

    private void validateQuestionAgainstGameType(GameType gameType, QuestionRequest request) {
        if (gameType == GameType.IMAGE_RECOGNITION) {
            if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
                throw new BusinessRuleException("imageUrl is required for IMAGE_RECOGNITION questions");
            }
        }
        if (gameType == GameType.MEMORY_MATCH || gameType == GameType.PUZZLE) {
            if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
                throw new BusinessRuleException("imageUrl is required for MEMORY_MATCH/PUZZLE cards");
            }
            if (request.getCorrectAnswer() == null || request.getCorrectAnswer().isBlank()) {
                throw new BusinessRuleException("correctAnswer (pair id) is required for MEMORY_MATCH/PUZZLE cards");
            }
        }
    }
}
