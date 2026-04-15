package tn.SoftCare.Geofencing.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.SoftCare.Geofencing.Entity.Zone;
import java.util.List;
import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {

    // Pour la vérification GPS (un patient → une zone)
    Optional<Zone> findByPatientId(String patientId);

    // Toutes les zones d'un patient
    List<Zone> findAllByPatientId(String patientId);

    // Toutes les zones gérées par un soignant
    List<Zone> findBySoignantId(String soignantId);
}