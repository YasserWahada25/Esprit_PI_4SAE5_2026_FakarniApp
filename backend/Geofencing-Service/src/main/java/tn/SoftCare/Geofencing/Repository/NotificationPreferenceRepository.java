package tn.SoftCare.Geofencing.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.SoftCare.Geofencing.Entity.NotificationPreference;
import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findBySoignantId(String soignantId);
}