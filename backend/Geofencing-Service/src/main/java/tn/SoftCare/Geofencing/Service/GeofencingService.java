package tn.SoftCare.Geofencing.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.SoftCare.Geofencing.Client.TrackingClient;
import tn.SoftCare.Geofencing.Entity.Zone;
import tn.SoftCare.Geofencing.Repository.ZoneRepository;
import tn.SoftCare.Geofencing.dto.PositionDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class GeofencingService {

    private static final long OFFLINE_THRESHOLD_MINUTES = 5;

    @Autowired private ZoneRepository  zoneRepository;
    @Autowired private AlertService    alertService;
    @Autowired private TrackingClient  trackingClient;   // ← nouveau

    // ─── CRUD Zones ───────────────────────────────────────────────

    public Zone addZone(Zone z) { return zoneRepository.save(z); }

    public List<Zone> getAll() { return zoneRepository.findAll(); }

    public List<Zone> getZonesByPatient(String patientId) {
        return zoneRepository.findAllByPatientId(patientId);
    }

    public List<Zone> getZonesBySoignant(String soignantId) {
        return zoneRepository.findBySoignantId(soignantId);
    }

    public void deleteZone(Long id) { zoneRepository.deleteById(id); }

    public Zone updateZone(Long id, Zone details) {
        return zoneRepository.findById(id).map(z -> {
            z.setNomZone(details.getNomZone());
            z.setCentreLat(details.getCentreLat());
            z.setCentreLon(details.getCentreLon());
            z.setRayon(details.getRayon());
            z.setType(details.getType());
            return zoneRepository.save(z);
        }).orElse(null);
    }

    // ─── Analyse GPS ──────────────────────────────────────────────

    public void verifierPosition(String patientId, double lat, double lon) {
        zoneRepository.findByPatientId(patientId).ifPresentOrElse(zone -> {

            double  distance    = haversine(lat, lon, zone.getCentreLat(), zone.getCentreLon());
            boolean enAlerte    = false;
            double  depassement = 0;
            String  typeAlerte  = "";

            if ("SAFE".equals(zone.getType().name())) {
                if (distance > zone.getRayon()) {
                    enAlerte    = true;
                    depassement = distance - zone.getRayon();
                    typeAlerte  = "SORTIE_ZONE_SAFE";
                }
            } else if ("DANGER".equals(zone.getType().name())) {
                if (distance < zone.getRayon()) {
                    enAlerte    = true;
                    depassement = zone.getRayon() - distance;
                    typeAlerte  = "ENTREE_ZONE_DANGER";
                }
            }

            if (enAlerte) {
                alertService.createAlert(
                        patientId,
                        zone.getSoignantId(),
                        depassement,
                        typeAlerte
                );
            } else {
                System.out.println("✅ " + patientId
                        + " en règle (" + Math.round(distance) + "m)");
            }

        }, () -> System.err.println("ℹ️ Aucune zone pour : " + patientId));
    }

    // ─── Détection Offline ────────────────────────────────────────

    @Scheduled(fixedDelay = 60_000)
    public void checkOfflinePatients() {
        LocalDateTime now = LocalDateTime.now();

        for (Zone zone : zoneRepository.findAll()) {
            String patientId = zone.getPatientId();
            if (patientId == null || patientId.isBlank()) continue;

            try {
                PositionDto pos = trackingClient.getLastPosition(patientId);
                if (pos == null || pos.getTimestamp() == null) continue;

                long minutesSince = ChronoUnit.MINUTES.between(pos.getTimestamp(), now);

                if (minutesSince >= OFFLINE_THRESHOLD_MINUTES) {
                    System.out.println("📴 Offline : " + patientId
                            + " (inactif depuis " + minutesSince + " min)");

                    alertService.createAlert(
                            patientId,
                            zone.getSoignantId(),
                            0,
                            "PATIENT_OFFLINE"
                    );
                }

            } catch (Exception e) {
                System.err.println("⚠️ Tracking injoignable pour "
                        + patientId + " : " + e.getMessage());
            }
        }
    }

    // ─── Haversine ────────────────────────────────────────────────

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R    = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a    = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}