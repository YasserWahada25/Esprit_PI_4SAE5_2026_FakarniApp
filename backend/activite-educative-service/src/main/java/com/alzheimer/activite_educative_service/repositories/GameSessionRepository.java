package com.alzheimer.activite_educative_service.repositories;

import com.alzheimer.activite_educative_service.entities.GameSession;
import com.alzheimer.activite_educative_service.entities.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    List<GameSession> findByUserIdOrderByStartedAtDesc(Long userId);

    Optional<GameSession> findFirstByUserIdAndActivity_IdAndStatusInOrderByFinishedAtDesc(
            Long userId,
            Long activityId,
            Collection<SessionStatus> statuses
    );
}
