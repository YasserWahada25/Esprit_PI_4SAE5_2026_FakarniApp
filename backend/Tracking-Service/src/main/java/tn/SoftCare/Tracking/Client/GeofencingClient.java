package tn.SoftCare.Tracking.Client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "GEOFENCING-SERVICE")
public interface GeofencingClient {
    @PostMapping("/api/geofencing/analyser")
    void envoyerPourAnalyse(@RequestBody Map<String, Object> payload);
}