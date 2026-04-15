package tn.SoftCare.Tracking.dto;

/**
 * DTO reçu depuis Angular (position GPS réelle du patient connecté)
 */
public class PositionRequest {

    private String patientId;  // userId MongoDB du patient connecté
    private double latitude;
    private double longitude;
    private double accuracy;   // précision GPS en mètres (optionnel)

    public PositionRequest() {}

    public String getPatientId()             { return patientId; }
    public void   setPatientId(String p)     { this.patientId = p; }
    public double getLatitude()              { return latitude; }
    public void   setLatitude(double lat)    { this.latitude = lat; }
    public double getLongitude()             { return longitude; }
    public void   setLongitude(double lon)   { this.longitude = lon; }
    public double getAccuracy()              { return accuracy; }
    public void   setAccuracy(double a)      { this.accuracy = a; }
}