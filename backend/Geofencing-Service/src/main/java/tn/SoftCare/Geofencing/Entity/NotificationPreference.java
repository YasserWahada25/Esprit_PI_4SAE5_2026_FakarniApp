package tn.SoftCare.Geofencing.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class NotificationPreference {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String soignantId;    // userId du soignant connecté
    private boolean emailEnabled; // recevoir email
    private boolean voiceEnabled; // recevoir appel vocal
}