package tn.SoftCare.Geofencing.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SoftCare.Geofencing.Client.UserClient;
import tn.SoftCare.Geofencing.Entity.Alert;
import tn.SoftCare.Geofencing.Entity.NotificationPreference;
import tn.SoftCare.Geofencing.Repository.AlertRepository;
import tn.SoftCare.Geofencing.Repository.NotificationPreferenceRepository;
import tn.SoftCare.Geofencing.dto.UserDto;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    @Autowired private AlertRepository                   alertRepository;
    @Autowired private NotificationService               notificationService;
    @Autowired private NotificationPreferenceRepository  prefRepository;
    @Autowired private UserClient                        userClient;

    public void createAlert(String patientId, String soignantId,
                            double distance, String typeAlerte) {

        // Éviter les doublons
        boolean dejaEnAlerte = alertRepository
                .findByPatientIdAndStatusAndType(patientId, "Active", typeAlerte)
                .isPresent();

        if (dejaEnAlerte) {
            System.out.println("⚠️ Alerte déjà active pour : " + patientId);
            return;
        }

        // Résoudre nom patient
        String patientName = resolveFullName(patientId);

        // Créer l'alerte
        Alert alerte = new Alert();
        alerte.setPatientId(patientId);
        alerte.setPatientName(patientName);
        alerte.setSoignantId(soignantId);
        alerte.setType(typeAlerte);
        alerte.setTimestamp(LocalDateTime.now());
        alerte.setStatus("Active");
        alerte.setSeverity(computeSeverity(distance));
        alerte.setDistanceHorsZone(distance);
        Alert saved = alertRepository.save(alerte);

        System.out.println("🚨 ALERTE : " + typeAlerte
                + " | " + patientName
                + " | " + Math.round(distance) + "m");

        // Notifier selon les préférences du soignant
        if (soignantId != null && !soignantId.isEmpty()) {
            try {
                UserDto soignant = userClient.getUserById(soignantId);

                // Récupérer les préférences — défaut: email ON, voice OFF
                NotificationPreference pref = prefRepository
                        .findBySoignantId(soignantId)
                        .orElseGet(() -> {
                            NotificationPreference def = new NotificationPreference();
                            def.setSoignantId(soignantId);
                            def.setEmailEnabled(true);
                            def.setVoiceEnabled(false);
                            return def;
                        });

                if (soignant != null) {
                    notificationService.notifySoignant(soignant, saved, pref);
                }

            } catch (Exception e) {
                System.err.println("⚠️ Notification impossible : " + e.getMessage());
            }
        }
    }

    public List<Alert> getAllAlerts()                          { return alertRepository.findAll(); }
    public List<Alert> getAlertsByPatient(String patientId)   { return alertRepository.findByPatientId(patientId); }
    public List<Alert> getAlertsBySoignant(String soignantId) { return alertRepository.findBySoignantId(soignantId); }

    public Alert resolveAlert(Long alertId) {
        return alertRepository.findById(alertId).map(a -> {
            a.setStatus("Resolved");
            return alertRepository.save(a);
        }).orElse(null);
    }

    private String resolveFullName(String userId) {
        try {
            UserDto user = userClient.getUserById(userId);
            return (user != null) ? user.getFullName() : userId;
        } catch (Exception e) {
            return userId;
        }
    }

    private String computeSeverity(double distance) {
        if (distance > 500) return "High";
        if (distance > 100) return "Medium";
        return "Low";
    }
}