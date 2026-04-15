package tn.SoftCare.Geofencing.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.SoftCare.Geofencing.Entity.Alert;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    // Alertes du patient connecté
    List<Alert> findByPatientId(String patientId);

    // Alertes supervisées par le soignant
    List<Alert> findBySoignantId(String soignantId);

    // Vérifier si alerte active déjà présente (éviter doublons)
    Optional<Alert> findByPatientIdAndStatus(String patientId, String status);
    Optional<Alert> findByPatientIdAndStatusAndType(String patientId, String status, String type);

}