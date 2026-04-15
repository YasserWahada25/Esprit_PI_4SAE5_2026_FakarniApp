package tn.SoftCare.Tracking.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SoftCare.Tracking.Client.GeofencingClient;
import tn.SoftCare.Tracking.Entity.Position;
import tn.SoftCare.Tracking.Repository.TrackingRepository;
import tn.SoftCare.Tracking.dto.PositionRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrackingService {

    @Autowired private TrackingRepository repository;
    @Autowired private GeofencingClient   geofencingClient;

    /**
     * Reçoit la position GPS réelle envoyée par Angular,
     * la sauvegarde et la transmet au Geofencing-Service.
     */
    public Position saveAndProcess(PositionRequest request) {

        // 1. Sauvegarder
        Position position = new Position();
        position.setPatientId(request.getPatientId());
        position.setLatitude(request.getLatitude());
        position.setLongitude(request.getLongitude());
        position.setTimestamp(LocalDateTime.now());
        Position saved = repository.save(position);

        System.out.println("📍 GPS reçu — patient: " + saved.getPatientId()
                + " | lat: " + saved.getLatitude()
                + " | lon: " + saved.getLongitude());

        // 2. Transmettre au Geofencing-Service
        Map<String, Object> payload = new HashMap<>();
        payload.put("patientId", saved.getPatientId());
        payload.put("latitude",  saved.getLatitude());
        payload.put("longitude", saved.getLongitude());

        try {
            geofencingClient.envoyerPourAnalyse(payload);
        } catch (Exception e) {
            System.err.println("!! Geofencing indisponible : " + e.getMessage());
        }

        return saved;
    }

    public List<Position> getAllLastPositions() {
        return repository.findAllLastPositions();
    }

    public Position getLastPosition(String patientId) {
        return repository.findTopByPatientIdOrderByTimestampDesc(patientId)
                .orElse(null);
    }
}