package tn.SoftCare.Geofencing.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Alert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientId;       // userId MongoDB du patient
    private String patientName;     // résolu depuis User-Service ← dynamique
    private String soignantId;      // userId MongoDB du soignant ← NOUVEAU

    private String type;            // SORTIE_ZONE_SAFE, ENTREE_ZONE_DANGER...
    private LocalDateTime timestamp;
    private String status;          // Active, Resolved
    private String severity;        // High, Medium, Low ← calculé auto
    private double distanceHorsZone;
}