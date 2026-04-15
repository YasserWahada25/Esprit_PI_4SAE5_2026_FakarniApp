package tn.SoftCare.Geofencing.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.SoftCare.Geofencing.Entity.NotificationPreference;
import tn.SoftCare.Geofencing.Repository.NotificationPreferenceRepository;

@RestController
@RequestMapping("/api/geofencing/preferences")
public class NotificationPreferenceController {

    @Autowired
    private NotificationPreferenceRepository prefRepository;

    /** Récupérer les préférences du soignant connecté */
    @GetMapping("/{soignantId}")
    public NotificationPreference getPreferences(@PathVariable String soignantId) {
        return prefRepository.findBySoignantId(soignantId)
                .orElseGet(() -> {
                    // Créer des préférences par défaut si pas encore configurées
                    NotificationPreference pref = new NotificationPreference();
                    pref.setSoignantId(soignantId);
                    pref.setEmailEnabled(true);   // email activé par défaut
                    pref.setVoiceEnabled(false);  // appel désactivé par défaut
                    return pref;
                });
    }

    /** Sauvegarder les préférences */
    @PostMapping
    public NotificationPreference savePreferences(@RequestBody NotificationPreference pref) {
        return prefRepository.findBySoignantId(pref.getSoignantId())
                .map(existing -> {
                    existing.setEmailEnabled(pref.isEmailEnabled());
                    existing.setVoiceEnabled(pref.isVoiceEnabled());
                    return prefRepository.save(existing);
                })
                .orElseGet(() -> prefRepository.save(pref));
    }
}