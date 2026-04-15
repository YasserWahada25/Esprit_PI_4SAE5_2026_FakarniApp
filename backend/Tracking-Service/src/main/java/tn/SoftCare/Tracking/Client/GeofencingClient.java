package tn.SoftCare.Tracking.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tn.SoftCare.Tracking.dto.PositionRequest;

import java.util.Map;

// On supprime l'URL. Le "name" DOIT être le spring.application.name du Geofencing-Service
@FeignClient(name = "GEOFENCING-SERVICE")
public interface GeofencingClient {
    @PostMapping("/api/geofencing/analyser")
    void envoyerPourAnalyse(@RequestBody Map<String, Object> payload);
}