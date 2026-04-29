package com.alzheimer.suivi_engagement_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "activite-educative-service", contextId = "suiviActivitySessions")
public interface ActivitySessionsFeignClient {

    @GetMapping("/api/game-sessions/history")
    List<GameSessionHistoryFeignDto> history(@RequestParam("patientId") String patientId);

    /** Sessions récentes (tous patients) pour alimenter le suivi sans dépendre de la liste User. */
    @GetMapping("/api/game-sessions/engagement-sessions")
    List<GameSessionHistoryFeignDto> engagementSessions(@RequestParam(value = "limit", defaultValue = "500") int limit);
}
