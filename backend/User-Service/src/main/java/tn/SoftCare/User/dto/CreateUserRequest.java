package tn.SoftCare.User.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tn.SoftCare.User.model.Role;

public class CreateUserRequest {

    @NotBlank(message = "First name is required.")
    @Pattern(
            regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ\\s'-]+$",
            message = "First name must not contain numbers."
    )
    private String nom;

    @NotBlank(message = "Last name is required.")
    @Pattern(
            regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ\\s'-]+$",
            message = "Last name must not contain numbers."
    )
    private String prenom;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password must be at least 6 characters long.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{6,}$",
            message = "Password must contain at least 1 uppercase letter, 1 number, and 1 special character."
    )
    private String password;

    @NotNull(message = "Role is required.")
    private Role role;

    @Pattern(
            regexp = "^[0-9]{8}$",
            message = "Phone number must contain exactly 8 digits."
    )
    private String numTel;

    @NotBlank(message = "Address is required.")
    private String adresse;

    // Getters & Setters

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getNumTel() { return numTel; }
    public void setNumTel(String numTel) { this.numTel = numTel; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
}