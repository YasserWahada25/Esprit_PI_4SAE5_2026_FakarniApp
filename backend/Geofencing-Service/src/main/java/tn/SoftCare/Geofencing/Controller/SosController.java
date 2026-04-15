package tn.SoftCare.Geofencing.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.SoftCare.Geofencing.Service.SosService;

import java.util.Map;

@RestController
@RequestMapping("/api/geofencing/sos")
public class SosController {

    @Autowired private SosService sosService;

    /**
     * Appelé quand le patient appuie sur le bouton SOS
     * Body: { "patientId": "...", "soignantId": "...", "latitude": 0.0, "longitude": 0.0 }
     */
    @PostMapping("/trigger")
    public Map<String, String> triggerSos(@RequestBody Map<String, Object> payload) {
        String patientId  = payload.get("patientId").toString();
        String soignantId = payload.get("soignantId").toString();
        double latitude   = Double.parseDouble(payload.get("latitude").toString());
        double longitude  = Double.parseDouble(payload.get("longitude").toString());

        sosService.handleSos(patientId, soignantId, latitude, longitude);

        return Map.of("status", "SOS envoyé avec succès");
    }
}