package com.alzheimer.suivi_engagement_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "Event-Service", contextId = "suiviEventParticipations")
public interface EventParticipationsFeignClient {

    @GetMapping("/api/events/participations")
    List<EventParticipationFeignDto> listParticipations(
            @RequestParam(value = "patientId", required = false) String patientId
    );
}
