package com.alzheimer.event_service.repositories;

import com.alzheimer.event_service.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUserId(Long userId);

    // Trouver les événements à rappeler dans une fenêtre de temps
    List<Event> findByRemindEnabledTrueAndRemindSentFalseAndStartDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
