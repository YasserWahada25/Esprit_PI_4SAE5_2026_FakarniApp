package com.alzheimer.activite_educative_service.services;

import com.alzheimer.activite_educative_service.dto.*;
import com.alzheimer.activite_educative_service.entities.*;
import com.alzheimer.activite_educative_service.exceptions.BusinessRuleException;
import com.alzheimer.activite_educative_service.exceptions.ResourceNotFoundException;
import com.alzheimer.activite_educative_service.repositories.ActiviteEducativeRepository;
import com.alzheimer.activite_educative_service.repositories.EducationalQuestionRepository;
import com.alzheimer.activite_educative_service.repositories.GameSessionRepository;
import com.alzheimer.activite_educative_service.repositories.SessionAnswerRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GameSessionService {

    private static final Logger log = LoggerFactory.getLogger(GameSessionService.class);

    private final ActiviteEducativeRepository activiteEducativeRepository;
    private final GameSessionRepository gameSessionRepository;
    private final EducationalQuestionRepository questionRepository;
    private final SessionAnswerRepository sessionAnswerRepository;
    private final ObjectMapper objectMapper;
    private final PreconfiguredQuestionSeedService preconfiguredQuestionSeedService;

    public GameSessionService(
            ActiviteEducativeRepository activiteEducativeRepository,
            GameSessionRepository gameSessionRepository,
            EducationalQuestionRepository questionRepository,
            SessionAnswerRepository sessionAnswerRepository,
            ObjectMapper objectMapper,
            PreconfiguredQuestionSeedService preconfiguredQuestionSeedService
    ) {
        this.activiteEducativeRepository = activiteEducativeRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.questionRepository = questionRepository;
        this.sessionAnswerRepository = sessionAnswerRepository;
        this.objectMapper = objectMapper;
        this.preconfiguredQuestionSeedService = preconfiguredQuestionSeedService;
    }

    @Transactional
    public GameSessionStartResponse startGameSession(Long activityId, GameSessionStartRequest request) {
        log.info("Starting game session for activityId={}, userId={}", activityId, request.getUserId());
        ActiviteEducative activity = activiteEducativeRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));
        if (activity.getStatus() != ActivityStatus.ACTIVE) {
            throw new BusinessRuleException("Activity is not ACTIVE");
        }
        if (activity.getType() != ActivityType.GAME && activity.getType() != ActivityType.QUIZ) {
            throw new BusinessRuleException("Only GAME (or legacy QUIZ) activities support game sessions");
        }
        preconfiguredQuestionSeedService.ensureDefaultQuestions(activity);
        List<EducationalQuestion> questions = questionRepository.findByActivityIdOrderByOrderIndexAsc(activityId);
        if (questions.isEmpty()) {
            throw new BusinessRuleException(
                    "No questions for this activity: configure gameType (MEMORY_QUIZ, IMAGE_RECOGNITION, MEMORY_MATCH) "
                            + "or add questions via the API");
        }

        GameSession session = new GameSession();
        session.setUserId(request.getUserId());
        session.setActivity(activity);
        session.setStatus(SessionStatus.IN_PROGRESS);
        session.setCorrectCount(0);
        session.setMovesCount(0);
        session.setStartedAt(LocalDateTime.now());

        GameType gt = activity.getGameType();
        if (gt == GameType.MEMORY_MATCH) {
            int pairs = validateAndCountMemoryPairs(questions);
            session.setTotalQuestions(pairs);
        } else {
            session.setTotalQuestions(questions.size());
        }

        GameSession saved = gameSessionRepository.save(session);

        GameSessionStartResponse resp = new GameSessionStartResponse();
        resp.setSessionId(saved.getId());
        resp.setActivityId(activityId);
        resp.setUserId(request.getUserId());
        resp.setStatus(saved.getStatus());
        resp.setTotalQuestions(saved.getTotalQuestions());
        resp.setGameType(activity.getGameType());

        if (gt == GameType.MEMORY_MATCH) {
            resp.setImageCards(buildShuffledImageCards(questions));
            resp.setQuestions(List.of());
        } else {
            resp.setQuestions(questions.stream().map(this::toPlayDto).collect(Collectors.toList()));
            resp.setImageCards(null);
        }
        return resp;
    }

    /**
     * Coup de memory : deux identifiants de cartes (questions). Enregistre une paire trouvée si match.
     */
    @Transactional
    public MemoryMoveResponse submitMemoryMove(Long sessionId, MemoryMoveRequest request) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new BusinessRuleException("Session is not in progress");
        }
        ActiviteEducative activity = activiteEducativeRepository.findById(session.getActivity().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));
        if (activity.getGameType() != GameType.MEMORY_MATCH) {
            throw new BusinessRuleException("Memory moves are only allowed for MEMORY_MATCH activities");
        }
        if (Objects.equals(request.getFirstCardId(), request.getSecondCardId())) {
            throw new BusinessRuleException("Select two different cards");
        }

        EducationalQuestion c1 = questionRepository.findById(request.getFirstCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + request.getFirstCardId()));
        EducationalQuestion c2 = questionRepository.findById(request.getSecondCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + request.getSecondCardId()));
        if (!c1.getActivity().getId().equals(activity.getId()) || !c2.getActivity().getId().equals(activity.getId())) {
            throw new BusinessRuleException("Cards do not belong to this activity");
        }

        int moves = session.getMovesCount() != null ? session.getMovesCount() : 0;
        moves++;
        session.setMovesCount(moves);
        gameSessionRepository.save(session);

        MemoryMoveResponse out = new MemoryMoveResponse();
        out.setMovesCount(moves);
        out.setPairTotal(session.getTotalQuestions());

        boolean samePair = isSameMemoryPair(c1, c2);
        out.setMatch(samePair);

        int pairsFound = (int) sessionAnswerRepository.countBySessionId(sessionId);
        out.setPairsFound(pairsFound);

        if (!samePair) {
            out.setGameCompleted(false);
            return out;
        }

        long minQid = Math.min(c1.getId(), c2.getId());
        long maxQid = Math.max(c1.getId(), c2.getId());
        if (sessionAnswerRepository.findBySessionIdAndQuestionId(sessionId, minQid).isPresent()) {
            out.setMatch(true);
            out.setPairsFound(pairsFound);
            out.setGameCompleted(session.getStatus() != SessionStatus.IN_PROGRESS);
            return out;
        }

        EducationalQuestion canonical = questionRepository.getReferenceById(minQid);
        SessionAnswer sa = new SessionAnswer();
        sa.setSession(session);
        sa.setQuestion(canonical);
        sa.setUserAnswer("PAIR:" + maxQid);
        sa.setCorrect(true);
        sa.setAnsweredAt(LocalDateTime.now());
        sessionAnswerRepository.save(sa);

        pairsFound = (int) sessionAnswerRepository.countBySessionId(sessionId);
        out.setPairsFound(pairsFound);

        if (pairsFound >= session.getTotalQuestions()) {
            GameSessionResultResponse result = finalizeSession(session);
            out.setGameCompleted(true);
            out.setSessionResult(result);
        } else {
            out.setGameCompleted(false);
        }
        return out;
    }

    @Transactional
    public SubmitAnswerResponse submitAnswerForActivity(Long activityId, ActivitySubmitAnswerRequest body) {
        GameSession session = gameSessionRepository.findById(body.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + body.getSessionId()));
        if (!session.getActivity().getId().equals(activityId)) {
            throw new BusinessRuleException("Session does not belong to this activity");
        }
        SubmitAnswerRequest inner = new SubmitAnswerRequest();
        inner.setQuestionId(body.getQuestionId());
        inner.setAnswer(body.getAnswer());
        return submitAnswer(body.getSessionId(), inner);
    }

    @Transactional
    public SubmitAnswerResponse submitAnswer(Long sessionId, SubmitAnswerRequest request) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new BusinessRuleException("Session is not in progress");
        }
        ActiviteEducative act = activiteEducativeRepository.findById(session.getActivity().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));
        if (act.getGameType() == GameType.MEMORY_MATCH) {
            throw new BusinessRuleException("Use POST /api/game-sessions/{sessionId}/move for MEMORY_MATCH");
        }

        EducationalQuestion question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + request.getQuestionId()));
        if (!question.getActivity().getId().equals(session.getActivity().getId())) {
            throw new BusinessRuleException("Question does not belong to this session's activity");
        }

        boolean ok = matches(question.getCorrectAnswer(), request.getAnswer());
        SessionAnswer sa = sessionAnswerRepository
                .findBySessionIdAndQuestionId(sessionId, question.getId())
                .orElseGet(SessionAnswer::new);
        sa.setSession(session);
        sa.setQuestion(question);
        sa.setUserAnswer(request.getAnswer());
        sa.setCorrect(ok);
        sa.setAnsweredAt(LocalDateTime.now());
        sessionAnswerRepository.save(sa);

        long correct = sessionAnswerRepository.countBySessionIdAndCorrectTrue(sessionId);
        long answered = sessionAnswerRepository.countBySessionId(sessionId);

        SubmitAnswerResponse r = new SubmitAnswerResponse();
        r.setCorrect(ok);
        r.setCorrectAnswersSoFar((int) correct);
        r.setAnsweredCount((int) answered);

        if (answered >= session.getTotalQuestions()) {
            GameSessionResultResponse done = finalizeSession(session);
            r.setSessionFinished(true);
            r.setSessionResult(done);
        }
        return r;
    }

    @Transactional
    public GameSessionResultResponse finishSession(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new BusinessRuleException("Session is not in progress");
        }
        return finalizeSession(session);
    }

    private GameSessionResultResponse finalizeSession(GameSession session) {
        long correct = sessionAnswerRepository.countBySessionIdAndCorrectTrue(session.getId());
        int total = session.getTotalQuestions();
        double percentage = total == 0 ? 0.0 : (correct * 100.0 / total);
        ActiviteEducative activity = activiteEducativeRepository.findById(session.getActivity().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));
        double threshold = resolvePassThreshold(activity);
        session.setStatus(percentage >= threshold ? SessionStatus.SUCCESS : SessionStatus.FAILURE);
        session.setCorrectCount((int) correct);
        session.setScorePercent(percentage);
        session.setFinishedAt(LocalDateTime.now());
        gameSessionRepository.save(session);
        return buildResult(session);
    }

    private static double resolvePassThreshold(ActiviteEducative activity) {
        if (activity.getScoreThreshold() != null) {
            return activity.getScoreThreshold();
        }
        if (activity.getType() == ActivityType.GAME || activity.getType() == ActivityType.QUIZ) {
            return 60.0;
        }
        return 0.0;
    }

    @Transactional(readOnly = true)
    public GameSessionResultResponse getSessionResult(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        GameSessionResultResponse r = buildResult(session);
        long correct = sessionAnswerRepository.countBySessionIdAndCorrectTrue(sessionId);
        r.setCorrectCount((int) correct);
        if (isTerminal(session.getStatus())) {
            r.setScorePercent(session.getScorePercent());
        } else {
            r.setScorePercent(liveScore(session, correct));
        }
        return r;
    }

    private static boolean isTerminal(SessionStatus status) {
        return status == SessionStatus.SUCCESS
                || status == SessionStatus.FAILURE
                || status == SessionStatus.COMPLETED;
    }

    @Transactional(readOnly = true)
    public List<GameSessionHistoryItemResponse> getHistory(Long userId) {
        return gameSessionRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(this::toHistoryItem)
                .collect(Collectors.toList());
    }

    private GameSessionHistoryItemResponse toHistoryItem(GameSession s) {
        GameSessionHistoryItemResponse h = new GameSessionHistoryItemResponse();
        h.setSessionId(s.getId());
        h.setActivityId(s.getActivity().getId());
        h.setActivityTitle(s.getActivity().getTitle());
        h.setStatus(s.getStatus());
        h.setScorePercent(s.getScorePercent());
        h.setStartedAt(s.getStartedAt());
        h.setFinishedAt(s.getFinishedAt());
        return h;
    }

    private GameSessionResultResponse buildResult(GameSession session) {
        GameSessionResultResponse r = new GameSessionResultResponse();
        r.setSessionId(session.getId());
        r.setActivityId(session.getActivity().getId());
        r.setActivityTitle(session.getActivity().getTitle());
        r.setUserId(session.getUserId());
        r.setStatus(session.getStatus());
        r.setTotalQuestions(session.getTotalQuestions());
        r.setCorrectCount(session.getCorrectCount());
        r.setScorePercent(session.getScorePercent());
        r.setStartedAt(session.getStartedAt());
        r.setFinishedAt(session.getFinishedAt());
        r.setScore(session.getCorrectCount());
        r.setScoreMax(session.getTotalQuestions());
        r.setPercentage(session.getScorePercent());
        r.setDateCompleted(session.getFinishedAt());
        List<SessionAnswerDetailDto> details = new ArrayList<>();
        for (SessionAnswer a : sessionAnswerRepository.findBySessionIdOrderByAnsweredAtAsc(session.getId())) {
            EducationalQuestion q = a.getQuestion();
            SessionAnswerDetailDto d = new SessionAnswerDetailDto();
            d.setQuestionId(q.getId());
            d.setPrompt(q.getPrompt());
            d.setExpectedAnswer(q.getCorrectAnswer());
            d.setImageUrl(q.getImageUrl());
            d.setQuestionImageUrl(q.getImageUrl());
            d.setUserAnswer(a.getUserAnswer());
            d.setCorrect(Boolean.TRUE.equals(a.getCorrect()));
            details.add(d);
        }
        r.setAnswers(details);
        return r;
    }

    private static Double liveScore(GameSession session, long correct) {
        int total = session.getTotalQuestions();
        if (total == 0) {
            return 0.0;
        }
        return correct * 100.0 / total;
    }

    private QuestionPlayDto toPlayDto(EducationalQuestion q) {
        QuestionPlayDto dto = new QuestionPlayDto();
        dto.setId(q.getId());
        dto.setOrderIndex(q.getOrderIndex());
        dto.setPrompt(q.getPrompt());
        dto.setImageUrl(q.getImageUrl());
        dto.setQuestionImageUrl(q.getImageUrl());
        fillPlayOptions(dto, q);
        return dto;
    }

    private void fillPlayOptions(QuestionPlayDto dto, EducationalQuestion q) {
        String json = q.getOptionsJson();
        if (json == null || json.isBlank()) {
            dto.setOptions(List.of());
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.isArray() && root.size() > 0 && root.get(0).isObject()) {
                List<QuizOptionPlayDto> rich = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                for (JsonNode n : root) {
                    QuizOptionPlayDto opt = new QuizOptionPlayDto();
                    String label = n.path("label").asText("");
                    opt.setLabel(label);
                    JsonNode img = n.get("imageUrl");
                    if (img != null && !img.isNull() && !img.asText().isBlank()) {
                        opt.setImageUrl(img.asText());
                    }
                    rich.add(opt);
                    labels.add(label);
                }
                dto.setQuizOptions(rich);
                dto.setOptions(labels);
            } else {
                dto.setOptions(objectMapper.readValue(json, new TypeReference<List<String>>() {
                }));
            }
        } catch (Exception e) {
            log.debug("Fallback string options for question {}", q.getId(), e);
            dto.setOptions(parseLegacyStringOptions(json));
        }
    }

    private List<String> parseLegacyStringOptions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private static boolean isSameMemoryPair(EducationalQuestion a, EducationalQuestion b) {
        if (a.getCorrectAnswer() == null || b.getCorrectAnswer() == null) {
            return false;
        }
        return a.getCorrectAnswer().trim().equalsIgnoreCase(b.getCorrectAnswer().trim());
    }

    private static int validateAndCountMemoryPairs(List<EducationalQuestion> questions) {
        Map<String, Long> freq = new HashMap<>();
        for (EducationalQuestion q : questions) {
            String key = q.getCorrectAnswer() == null ? "" : q.getCorrectAnswer().trim();
            if (key.isEmpty()) {
                throw new BusinessRuleException("MEMORY_MATCH: correctAnswer must be a non-empty pair id on each card");
            }
            if (q.getImageUrl() == null || q.getImageUrl().isBlank()) {
                throw new BusinessRuleException("MEMORY_MATCH: each card must have an imageUrl");
            }
            freq.merge(key.toUpperCase(), 1L, Long::sum);
        }
        for (Map.Entry<String, Long> e : freq.entrySet()) {
            if (e.getValue() != 2) {
                throw new BusinessRuleException(
                        "MEMORY_MATCH: pair id \"" + e.getKey() + "\" must appear on exactly 2 cards");
            }
        }
        return freq.size();
    }

    private List<ImageCardPlayDto> buildShuffledImageCards(List<EducationalQuestion> questions) {
        List<ImageCardPlayDto> cards = new ArrayList<>();
        for (EducationalQuestion q : questions) {
            ImageCardPlayDto c = new ImageCardPlayDto();
            c.setId(q.getId());
            c.setImageUrl(q.getImageUrl());
            String prompt = q.getPrompt();
            c.setBackLabel(prompt != null && !prompt.isBlank() ? prompt : "Carte");
            cards.add(c);
        }
        Collections.shuffle(cards);
        return cards;
    }

    private static boolean matches(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return expected.trim().equalsIgnoreCase(actual.trim());
    }
}
