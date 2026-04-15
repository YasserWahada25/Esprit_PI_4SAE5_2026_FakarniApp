package tn.SoftCare.Tracking.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.SoftCare.Tracking.Entity.Position;

import java.util.List;
import java.util.Optional;

public interface TrackingRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByPatientId(String patientId);
    Optional<Position> findTopByPatientIdOrderByTimestampDesc(String patientId);

    @Query("""
        SELECT p FROM Position p
        WHERE p.id IN (
            SELECT MAX(p2.id) FROM Position p2 GROUP BY p2.patientId
        )
    """)
    List<Position> findAllLastPositions();

}
