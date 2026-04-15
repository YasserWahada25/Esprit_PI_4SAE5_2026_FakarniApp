package tn.SoftCare.Geofencing.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.SoftCare.Geofencing.Entity.Alert;
import tn.SoftCare.Geofencing.Service.AlertService;
import java.util.List;

@RestController
@RequestMapping("/api/geofencing/alerts")
public class AlertController {

    @Autowired private AlertService alertService;

    /** Toutes les alertes */
    @GetMapping
    public List<Alert> getAll() {
        return alertService.getAllAlerts();
    }

    /** Alertes du patient connecté */
    @GetMapping("/patient/{patientId}")
    public List<Alert> getByPatient(@PathVariable String patientId) {
        return alertService.getAlertsByPatient(patientId);
    }

    /** Alertes supervisées par le soignant connecté */
    @GetMapping("/soignant/{soignantId}")
    public List<Alert> getBySoignant(@PathVariable String soignantId) {
        return alertService.getAlertsBySoignant(soignantId);
    }

    /** Résoudre une alerte */
    @PutMapping("/{id}/resolve")
    public Alert resolve(@PathVariable Long id) {
        return alertService.resolveAlert(id);
    }
}