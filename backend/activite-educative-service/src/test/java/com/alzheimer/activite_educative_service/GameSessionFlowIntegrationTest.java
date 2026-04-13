package com.alzheimer.activite_educative_service;

import com.alzheimer.activite_educative_service.dto.*;
import com.alzheimer.activite_educative_service.entities.ActivityStatus;
import com.alzheimer.activite_educative_service.entities.ActivityType;
import com.alzheimer.activite_educative_service.entities.GameType;
import com.alzheimer.activite_educative_service.entities.SessionStatus;
import com.alzheimer.activite_educative_service.services.ActiviteEducativeService;
import com.alzheimer.activite_educative_service.services.EducationalQuestionService;
import com.alzheimer.activite_educative_service.services.GameSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GameSessionFlowIntegrationTest {

    @Autowired
    ActiviteEducativeService activiteEducativeService;

    @Autowired
    EducationalQuestionService educationalQuestionService;

    @Autowired
    GameSessionService gameSessionService;

    @Test
    void startSubmitFinish_computesScore() {
        ActiviteEducativeRequest ar = new ActiviteEducativeRequest();
        ar.setTitle("Test game");
        ar.setDescription("d");
        ar.setType(ActivityType.GAME);
        ar.setGameType(GameType.MEMORY_QUIZ);
        ar.setStatus(ActivityStatus.ACTIVE);
        ar.setScoreThreshold(60.0);
        var act = activiteEducativeService.createActivity(ar);
        Long aid = act.getId();

        QuestionRequest q1 = new QuestionRequest();
        q1.setOrderIndex(1);
        q1.setPrompt("2+2?");
        q1.setOptions(List.of("3", "4", "5"));
        q1.setCorrectAnswer("4");
        var q = educationalQuestionService.createQuestion(aid, q1);

        GameSessionStartRequest start = new GameSessionStartRequest();
        start.setUserId(99L);
        GameSessionStartResponse session = gameSessionService.startGameSession(aid, start);
        assertThat(session.getTotalQuestions()).isEqualTo(1);
        assertThat(session.getGameType()).isEqualTo(GameType.MEMORY_QUIZ);

        SubmitAnswerRequest ans = new SubmitAnswerRequest();
        ans.setQuestionId(q.getId());
        ans.setAnswer("4");
        SubmitAnswerResponse sub = gameSessionService.submitAnswer(session.getSessionId(), ans);
        assertThat(sub.isCorrect()).isTrue();
        assertThat(sub.isSessionFinished()).isTrue();
        assertThat(sub.getSessionResult().getStatus()).isEqualTo(SessionStatus.SUCCESS);
        assertThat(sub.getSessionResult().getScorePercent()).isEqualTo(100.0);
        assertThat(sub.getSessionResult().getAnswers()).hasSize(1);
        assertThat(sub.getSessionResult().getAnswers().get(0).getPrompt()).isEqualTo("2+2?");
        assertThat(sub.getSessionResult().getAnswers().get(0).getExpectedAnswer()).isEqualTo("4");
        assertThat(sub.getSessionResult().getAnswers().get(0).getUserAnswer()).isEqualTo("4");
        assertThat(sub.getSessionResult().getAnswers().get(0).isCorrect()).isTrue();
    }

    @Test
    void finish_belowThreshold_isFailure() {
        ActiviteEducativeRequest ar = new ActiviteEducativeRequest();
        ar.setTitle("Hard game");
        ar.setDescription("d");
        ar.setType(ActivityType.GAME);
        ar.setGameType(GameType.MEMORY_QUIZ);
        ar.setStatus(ActivityStatus.ACTIVE);
        ar.setScoreThreshold(100.0);
        var act = activiteEducativeService.createActivity(ar);
        Long aid = act.getId();

        QuestionRequest q1 = new QuestionRequest();
        q1.setOrderIndex(1);
        q1.setPrompt("2+2?");
        q1.setOptions(List.of("3", "4", "5"));
        q1.setCorrectAnswer("4");
        var q = educationalQuestionService.createQuestion(aid, q1);

        GameSessionStartRequest start = new GameSessionStartRequest();
        start.setUserId(100L);
        GameSessionStartResponse session = gameSessionService.startGameSession(aid, start);

        SubmitAnswerRequest ans = new SubmitAnswerRequest();
        ans.setQuestionId(q.getId());
        ans.setAnswer("3");
        SubmitAnswerResponse sub = gameSessionService.submitAnswer(session.getSessionId(), ans);
        assertThat(sub.isSessionFinished()).isTrue();
        assertThat(sub.getSessionResult().getStatus()).isEqualTo(SessionStatus.FAILURE);
        assertThat(sub.getSessionResult().getScorePercent()).isEqualTo(0.0);
    }

    @Test
    void startSession_seedsDefaultQuestions_whenActivityHasNone() {
        ActiviteEducativeRequest ar = new ActiviteEducativeRequest();
        ar.setTitle("Empty quiz");
        ar.setDescription("d");
        ar.setType(ActivityType.GAME);
        ar.setGameType(GameType.MEMORY_QUIZ);
        ar.setStatus(ActivityStatus.ACTIVE);
        ar.setScoreThreshold(60.0);
        var act = activiteEducativeService.createActivity(ar);

        GameSessionStartRequest start = new GameSessionStartRequest();
        start.setUserId(42L);
        GameSessionStartResponse session = gameSessionService.startGameSession(act.getId(), start);
        assertThat(session.getTotalQuestions()).isEqualTo(3);
        assertThat(session.getQuestions()).hasSize(3);
    }

    @Test
    void memoryMatch_pairCompletesSession() {
        ActiviteEducativeRequest ar = new ActiviteEducativeRequest();
        ar.setTitle("Memory test");
        ar.setDescription("d");
        ar.setType(ActivityType.GAME);
        ar.setGameType(GameType.MEMORY_MATCH);
        ar.setStatus(ActivityStatus.ACTIVE);
        ar.setScoreThreshold(100.0);
        var act = activiteEducativeService.createActivity(ar);
        Long aid = act.getId();

        QuestionRequest c1 = new QuestionRequest();
        c1.setOrderIndex(1);
        c1.setPrompt("Carte A");
        c1.setImageUrl("https://example.com/a.png");
        c1.setOptions(List.of());
        c1.setCorrectAnswer("PAIR_X");
        var q1 = educationalQuestionService.createQuestion(aid, c1);

        QuestionRequest c2 = new QuestionRequest();
        c2.setOrderIndex(2);
        c2.setPrompt("Carte A");
        c2.setImageUrl("https://example.com/b.png");
        c2.setOptions(List.of());
        c2.setCorrectAnswer("PAIR_X");
        var q2 = educationalQuestionService.createQuestion(aid, c2);

        GameSessionStartRequest start = new GameSessionStartRequest();
        start.setUserId(77L);
        GameSessionStartResponse session = gameSessionService.startGameSession(aid, start);
        assertThat(session.getTotalQuestions()).isEqualTo(1);
        assertThat(session.getImageCards()).hasSize(2);
        assertThat(session.getQuestions()).isEmpty();

        MemoryMoveRequest mv = new MemoryMoveRequest();
        mv.setFirstCardId(q1.getId());
        mv.setSecondCardId(q2.getId());
        MemoryMoveResponse mr = gameSessionService.submitMemoryMove(session.getSessionId(), mv);
        assertThat(mr.isMatch()).isTrue();
        assertThat(mr.isGameCompleted()).isTrue();
        assertThat(mr.getSessionResult().getStatus()).isEqualTo(SessionStatus.SUCCESS);
        assertThat(mr.getSessionResult().getScorePercent()).isEqualTo(100.0);
    }
}
