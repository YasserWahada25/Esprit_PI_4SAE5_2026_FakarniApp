package tn.SoftCare.Geofencing.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SoftCare.Geofencing.Client.UserClient;
import tn.SoftCare.Geofencing.dto.UserDto;

@Service
public class SosService {

    @Autowired private UserClient            userClient;
    @Autowired private NotificationService   notificationService;

    /**
     * Déclenché quand le patient appuie sur le bouton SOS.
     * 1. Appel vocal immédiat vers le soignant
     * 2. Email au soignant avec la position du patient
     */
    public void handleSos(String patientId, String soignantId,
                          double latitude, double longitude) {

        System.out.println("🆘 SOS déclenché par : " + patientId);

        try {
            // Résoudre les noms
            UserDto patient  = getUserSafe(patientId);
            UserDto soignant = getUserSafe(soignantId);

            if (soignant == null) {
                System.err.println("❌ Soignant introuvable : " + soignantId);
                return;
            }

            String patientName = patient != null ? patient.getFullName() : patientId;

            // 1. Appel vocal immédiat vers le soignant
            if (soignant.getNumTel() != null && !soignant.getNumTel().isEmpty()) {
                notificationService.sendSosVoiceCall(
                        soignant.getNumTel(), patientName
                );
            } else {
                System.err.println("⚠️ Téléphone soignant absent — appel SOS ignoré");
            }

            // 2. Email SOS avec position GPS
            if (soignant.getEmail() != null && !soignant.getEmail().isEmpty()) {
                notificationService.sendSosEmail(
                        soignant.getEmail(), patientName, patientId, latitude, longitude
                );
            } else {
                System.err.println("⚠️ Email soignant absent — email SOS ignoré");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur SOS : " + e.getMessage());
        }
    }

    private UserDto getUserSafe(String userId) {
        try {
            return userClient.getUserById(userId);
        } catch (Exception e) {
            System.err.println("⚠️ User introuvable : " + userId);
            return null;
        }
    }
}