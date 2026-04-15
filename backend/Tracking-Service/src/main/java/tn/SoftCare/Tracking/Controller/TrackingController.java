package tn.SoftCare.Tracking.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.SoftCare.Tracking.Entity.Position;
import tn.SoftCare.Tracking.Service.TrackingService;
import tn.SoftCare.Tracking.dto.PositionRequest;
import java.util.List;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    @Autowired private TrackingService service;

    /**
     * Reçoit la position GPS réelle depuis Angular.
     * Body: { patientId, latitude, longitude, accuracy }
     */
    @PostMapping("/add")
    public Position ajouterPosition(@RequestBody PositionRequest request) {
        return service.saveAndProcess(request);
    }

    /** Dernière position de TOUS les patients */
    @GetMapping("/last")
    public List<Position> getAllLastPositions() {
        return service.getAllLastPositions();
    }

    /** Dernière position d'UN patient */
    @GetMapping("/last/{patientId}")
    public Position getLastPosition(@PathVariable String patientId) {
        return service.getLastPosition(patientId);
    }
}