package com.alzheimer.activite_educative_service.repositories;

import com.alzheimer.activite_educative_service.entities.SessionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionAnswerRepository extends JpaRepository<SessionAnswer, Long> {

    List<SessionAnswer> findBySessionIdOrderByAnsweredAtAsc(Long sessionId);

    Optional<SessionAnswer> findBySessionIdAndQuestionId(Long sessionId, Long questionId);

    long countBySessionIdAndCorrectTrue(Long sessionId);

    long countBySessionId(Long sessionId);
}
