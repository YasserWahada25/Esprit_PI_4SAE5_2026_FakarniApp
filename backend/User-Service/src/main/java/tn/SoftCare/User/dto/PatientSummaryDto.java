package tn.SoftCare.User.dto;

import tn.SoftCare.User.model.User;

/**
 * Vue inter-services / dashboard : identité patient alignée sur les champs métier.
 */
public class PatientSummaryDto {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;

    public static PatientSummaryDto fromUser(User u) {
        PatientSummaryDto d = new PatientSummaryDto();
        d.setId(u.getId());
        d.setFirstName(u.getPrenom());
        d.setLastName(u.getNom());
        d.setEmail(u.getEmail());
        d.setRole(u.getRole() != null ? u.getRole().name() : null);
        return d;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
