package com.alzheimer.event_service.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_participations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_event_patient", columnNames = {"event_id", "patient_id"})
})
public class EventParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "patient_id", nullable = false, length = 64)
    private String patientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EventParticipationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.registeredAt = LocalDateTime.now();
        this.updatedAt = this.registeredAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public EventParticipationStatus getStatus() {
        return status;
    }

    public void setStatus(EventParticipationStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
