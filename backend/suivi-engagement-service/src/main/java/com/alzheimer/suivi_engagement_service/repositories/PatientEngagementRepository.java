package com.alzheimer.suivi_engagement_service.repositories;

import com.alzheimer.suivi_engagement_service.entities.PatientEngagement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientEngagementRepository extends JpaRepository<PatientEngagement, Long> {
    List<PatientEngagement> findByPatientId(String patientId);
}

