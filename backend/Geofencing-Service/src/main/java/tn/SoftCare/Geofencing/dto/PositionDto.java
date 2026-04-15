package tn.SoftCare.Geofencing.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PositionDto {
    private String        patientId;
    private double        latitude;
    private double        longitude;
    private LocalDateTime timestamp;
}