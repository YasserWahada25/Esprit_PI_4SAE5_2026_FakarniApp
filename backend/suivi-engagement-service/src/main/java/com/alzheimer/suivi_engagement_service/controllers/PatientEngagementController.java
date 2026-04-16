package com.alzheimer.suivi_engagement_service.controllers;

import com.alzheimer.suivi_engagement_service.dto.EngagementDistributionResponse;
import com.alzheimer.suivi_engagement_service.dto.EngagementRowResponse;
import com.alzheimer.suivi_engagement_service.dto.EngagementStatsResponse;
import com.alzheimer.suivi_engagement_service.dto.EngagementSummaryResponse;
import com.alzheimer.suivi_engagement_service.dto.MlDatasetRowResponse;
import com.alzheimer.suivi_engagement_service.services.PatientEngagementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/engagement", "/engagement"})
public class PatientEngagementController {

    private final PatientEngagementService service;

    public PatientEngagementController(PatientEngagementService service) {
        this.service = service;
    }

    /** Liste plate (rétrocompat front existant). */
    @GetMapping
    public List<EngagementRowResponse> getAllEngagements() {
        return service.getAllEngagements();
    }

    @GetMapping("/patients")
    public List<EngagementRowResponse> listPatientsEngagement() {
        return service.getAllEngagements();
    }

    @GetMapping("/stats")
    public EngagementStatsResponse stats() {
        return service.getStats();
    }

    @GetMapping("/summary")
    public EngagementSummaryResponse summary() {
        return service.getSummary();
    }

    @GetMapping("/by-type")
    public Map<String, Long> byType() {
        return service.getByType();
    }

    @GetMapping("/distribution")
    public EngagementDistributionResponse distribution() {
        return service.getDistribution();
    }

    @GetMapping("/ml-dataset")
    public List<MlDatasetRowResponse> mlDataset() {
        return service.getMlDataset();
    }

    @GetMapping("/patients/{patientId}")
    public List<EngagementRowResponse> patientDetail(@PathVariable String patientId) {
        return service.getEngagementByPatientId(patientId);
    }

    @GetMapping("/patients/{patientId}/activities")
    public List<EngagementRowResponse> patientActivities(@PathVariable String patientId) {
        return service.getEngagementByPatientId(patientId);
    }

    @GetMapping("/{patientId}")
    public List<EngagementRowResponse> byPatient(@PathVariable String patientId) {
        return service.getEngagementByPatientId(patientId);
    }
}
