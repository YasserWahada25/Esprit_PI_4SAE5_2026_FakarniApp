package com.alzheimer.Event_Service.repositories;

import com.alzheimer.Event_Service.entities.EventParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventParticipationRepository extends JpaRepository<EventParticipation, Long> {

    List<EventParticipation> findByPatientIdOrderByRegisteredAtDesc(String patientId);

    List<EventParticipation> findAllByOrderByRegisteredAtDesc();

    boolean existsByEvent_IdAndPatientId(Long eventId, String patientId);

    void deleteAllByEvent_Id(Long eventId);
}

