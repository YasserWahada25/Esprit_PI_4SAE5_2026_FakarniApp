package tn.SoftCare.Geofencing.dto;

public class UserDto {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String numTel;

    public UserDto() {}

    public String getId()                   { return id; }
    public void   setId(String id)          { this.id = id; }
    public String getNom()                  { return nom; }
    public void   setNom(String nom)        { this.nom = nom; }
    public String getPrenom()               { return prenom; }
    public void   setPrenom(String prenom)  { this.prenom = prenom; }
    public String getEmail()                { return email; }
    public void   setEmail(String email)    { this.email = email; }
    public String getRole()                 { return role; }
    public void   setRole(String role)      { this.role = role; }
    public String getNumTel()               { return numTel; }
    public void   setNumTel(String numTel)  { this.numTel = numTel; }

    public String getFullName() {
        return ((prenom != null ? prenom : "") + " " + (nom != null ? nom : "")).trim();
    }
}