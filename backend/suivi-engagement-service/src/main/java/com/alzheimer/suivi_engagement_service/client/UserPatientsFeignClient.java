package com.alzheimer.suivi_engagement_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "User-Service", contextId = "suiviUserPatients")
public interface UserPatientsFeignClient {

    @GetMapping("/internal/users/patients")
    List<PatientSummaryFeignDto> listPatients();
}
