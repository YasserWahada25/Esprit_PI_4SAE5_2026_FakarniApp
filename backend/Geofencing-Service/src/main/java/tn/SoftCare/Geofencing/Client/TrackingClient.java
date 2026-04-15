package tn.SoftCare.Geofencing.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.SoftCare.Geofencing.dto.PositionDto;

@FeignClient(name = "TRACKING-SERVICE")
public interface TrackingClient {

    @GetMapping("/api/tracking/last/{patientId}")
    PositionDto getLastPosition(@PathVariable("patientId") String patientId);
}