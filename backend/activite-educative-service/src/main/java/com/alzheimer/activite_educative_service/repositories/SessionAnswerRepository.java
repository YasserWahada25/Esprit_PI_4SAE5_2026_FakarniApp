package com.alzheimer.activite_educative_service.repositories;

import com.alzheimer.activite_educative_service.entities.SessionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SessionAnswerRepository extends JpaRepository<SessionAnswer, Long> {

    List<SessionAnswer> findBySessionIdOrderByAnsweredAtAsc(Long sessionId);

    Optional<SessionAnswer> findBySessionIdAndQuestionId(Long sessionId, Long questionId);

    long countBySessionIdAndCorrectTrue(Long sessionId);

    long countBySessionId(Long sessionId);

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM SessionAnswer sa
            WHERE sa.session.activity.id = :activityId
               OR sa.question.activity.id = :activityId
            """)
    void deleteAllForActivity(@Param("activityId") Long activityId);
}
