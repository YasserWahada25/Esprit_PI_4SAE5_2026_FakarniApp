package com.alzheimer.activite_educative_service.repositories;

import com.alzheimer.activite_educative_service.entities.GameSession;
import com.alzheimer.activite_educative_service.entities.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    List<GameSession> findByPatientIdOrderByStartedAtDesc(String patientId);

    Optional<GameSession> findFirstByPatientIdAndActivity_IdAndStatusInOrderByFinishedAtDesc(
            String patientId,
            Long activityId,
            Collection<SessionStatus> statuses
    );

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM GameSession gs WHERE gs.activity.id = :activityId")
    void deleteAllForActivity(@Param("activityId") Long activityId);
}
