package tn.SoftCare.Geofencing.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import tn.SoftCare.Geofencing.Client.UserClient;
import tn.SoftCare.Geofencing.Entity.Alert;
import tn.SoftCare.Geofencing.Entity.NotificationPreference;
import tn.SoftCare.Geofencing.Repository.AlertRepository;
import tn.SoftCare.Geofencing.Repository.NotificationPreferenceRepository;
import tn.SoftCare.Geofencing.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires — AlertService.
 * UserClient et NotificationService sont mockés : aucun appel réseau/mail/Twilio.
 */
class AlertServiceTest {

    @InjectMocks
    private AlertService alertService;

    @Mock private AlertRepository                  alertRepository;
    @Mock private NotificationService              notificationService;
    @Mock private NotificationPreferenceRepository prefRepository;
    @Mock private UserClient                       userClient;

    // ── Helper ────────────────────────────────────────────────────────────────
    private Alert alert(Long id, String patientId, String soignantId, String status) {
        Alert a = new Alert();
        a.setId(id);
        a.setPatientId(patientId);
        a.setPatientName("Patient Test");
        a.setSoignantId(soignantId);
        a.setType("SORTIE_ZONE_SAFE");
        a.setStatus(status);
        a.setSeverity("Medium");
        a.setDistanceHorsZone(150.0);
        a.setTimestamp(LocalDateTime.of(2026, 4, 23, 10, 0));
        return a;
    }

    private UserDto userDto(String id, String fullName, String email) {
        UserDto u = new UserDto();
        u.setId(id);
        u.setFullName(fullName);
        u.setEmail(email);
        return u;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ── getAllAlerts ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllAlerts → retourne toutes les alertes")
    void getAllAlerts_returnsAll() {
        when(alertRepository.findAll()).thenReturn(List.of(
                alert(1L, "p1", "s1", "Active"),
                alert(2L, "p2", "s1", "Resolved")
        ));

        List<Alert> result = alertService.getAllAlerts();

        assertThat(result).hasSize(2);
        verify(alertRepository).findAll();
    }

    @Test
    @DisplayName("getAllAlerts → liste vide si aucune alerte")
    void getAllAlerts_returnsEmpty_whenNone() {
        when(alertRepository.findAll()).thenReturn(List.of());

        assertThat(alertService.getAllAlerts()).isEmpty();
    }

    // ── getAlertsByPatient ────────────────────────────────────────────────────

    @Test
    @DisplayName("getAlertsByPatient → filtre par patientId")
    void getAlertsByPatient_filtersCorrectly() {
        when(alertRepository.findByPatientId("p1"))
                .thenReturn(List.of(alert(1L, "p1", "s1", "Active")));

        List<Alert> result = alertService.getAlertsByPatient("p1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo("p1");
    }

    @Test
    @DisplayName("getAlertsByPatient → vide si patient sans alertes")
    void getAlertsByPatient_returnsEmpty() {
        when(alertRepository.findByPatientId("inconnu")).thenReturn(List.of());

        assertThat(alertService.getAlertsByPatient("inconnu")).isEmpty();
    }

    // ── getAlertsBySoignant ───────────────────────────────────────────────────

    @Test
    @DisplayName("getAlertsBySoignant → filtre par soignantId")
    void getAlertsBySoignant_filtersCorrectly() {
        when(alertRepository.findBySoignantId("s1")).thenReturn(List.of(
                alert(1L, "p1", "s1", "Active"),
                alert(2L, "p2", "s1", "Active")
        ));

        List<Alert> result = alertService.getAlertsBySoignant("s1");

        assertThat(result).hasSize(2);
        result.forEach(a -> assertThat(a.getSoignantId()).isEqualTo("s1"));
    }

    // ── resolveAlert ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("resolveAlert → passe le status à Resolved")
    void resolveAlert_setsStatusResolved() {
        Alert active = alert(1L, "p1", "s1", "Active");
        when(alertRepository.findById(1L)).thenReturn(Optional.of(active));
        when(alertRepository.save(any(Alert.class))).thenAnswer(i -> i.getArgument(0));

        Alert result = alertService.resolveAlert(1L);

        assertThat(result.getStatus()).isEqualTo("Resolved");
        verify(alertRepository).save(active);
    }

    @Test
    @DisplayName("resolveAlert → retourne null si alerte introuvable")
    void resolveAlert_returnsNull_whenNotFound() {
        when(alertRepository.findById(99L)).thenReturn(Optional.empty());

        Alert result = alertService.resolveAlert(99L);

        assertThat(result).isNull();
        verify(alertRepository, never()).save(any());
    }

    // ── createAlert ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createAlert → crée et sauvegarde l'alerte si pas de doublon")
    void createAlert_savesAlert_whenNoDuplicate() {
        when(alertRepository.findByPatientIdAndStatusAndType("p1", "Active", "SORTIE_ZONE_SAFE"))
                .thenReturn(Optional.empty());
        when(userClient.getUserById("p1"))
                .thenReturn(userDto("p1", "Patient Un", "p1@test.com"));
        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(prefRepository.findBySoignantId("s1"))
                .thenReturn(Optional.of(prefWith(true, false)));
        when(userClient.getUserById("s1"))
                .thenReturn(userDto("s1", "Soignant Un", "s1@test.com"));

        alertService.createAlert("p1", "s1", 200.0, "SORTIE_ZONE_SAFE");

        verify(alertRepository).save(argThat(a ->
                "p1".equals(a.getPatientId()) &&
                        "Active".equals(a.getStatus()) &&
                        "SORTIE_ZONE_SAFE".equals(a.getType())
        ));
    }

    @Test
    @DisplayName("createAlert → ignore si une alerte Active du même type existe déjà")
    void createAlert_skipsDuplicate() {
        when(alertRepository.findByPatientIdAndStatusAndType("p1", "Active", "SORTIE_ZONE_SAFE"))
                .thenReturn(Optional.of(alert(1L, "p1", "s1", "Active")));

        alertService.createAlert("p1", "s1", 200.0, "SORTIE_ZONE_SAFE");

        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("createAlert → severity High si distance > 500m")
    void createAlert_severityHigh_whenDistanceOver500() {
        when(alertRepository.findByPatientIdAndStatusAndType(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(userClient.getUserById(any())).thenReturn(null);
        when(alertRepository.save(any(Alert.class))).thenAnswer(i -> i.getArgument(0));

        alertService.createAlert("p1", "s1", 600.0, "SORTIE_ZONE_SAFE");

        verify(alertRepository).save(argThat(a -> "High".equals(a.getSeverity())));
    }

    @Test
    @DisplayName("createAlert → severity Medium si 100 < distance <= 500")
    void createAlert_severityMedium_whenDistanceBetween100And500() {
        when(alertRepository.findByPatientIdAndStatusAndType(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(userClient.getUserById(any())).thenReturn(null);
        when(alertRepository.save(any(Alert.class))).thenAnswer(i -> i.getArgument(0));

        alertService.createAlert("p1", "s1", 300.0, "SORTIE_ZONE_SAFE");

        verify(alertRepository).save(argThat(a -> "Medium".equals(a.getSeverity())));
    }

    @Test
    @DisplayName("createAlert → severity Low si distance <= 100m")
    void createAlert_severityLow_whenDistanceUnder100() {
        when(alertRepository.findByPatientIdAndStatusAndType(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(userClient.getUserById(any())).thenReturn(null);
        when(alertRepository.save(any(Alert.class))).thenAnswer(i -> i.getArgument(0));

        alertService.createAlert("p1", "s1", 50.0, "SORTIE_ZONE_SAFE");

        verify(alertRepository).save(argThat(a -> "Low".equals(a.getSeverity())));
    }

    @Test
    @DisplayName("createAlert → notifie le soignant si email activé")
    void createAlert_notifiesSoignant_whenEmailEnabled() {
        when(alertRepository.findByPatientIdAndStatusAndType(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(userClient.getUserById("p1"))
                .thenReturn(userDto("p1", "Patient Un", "p1@test.com"));
        when(alertRepository.save(any(Alert.class))).thenAnswer(i -> i.getArgument(0));
        when(prefRepository.findBySoignantId("s1"))
                .thenReturn(Optional.of(prefWith(true, false)));
        UserDto soignant = userDto("s1", "Soignant Un", "s1@test.com");
        when(userClient.getUserById("s1")).thenReturn(soignant);

        alertService.createAlert("p1", "s1", 200.0, "SORTIE_ZONE_SAFE");

        verify(notificationService).notifySoignant(
                eq(soignant), any(Alert.class), any(NotificationPreference.class));
    }

    @Test
    @DisplayName("createAlert → pas de notification si soignant null/vide")
    void createAlert_skipsNotification_whenNoSoignant() {
        when(alertRepository.findByPatientIdAndStatusAndType(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(userClient.getUserById("p1")).thenReturn(null);
        when(alertRepository.save(any(Alert.class))).thenAnswer(i -> i.getArgument(0));

        alertService.createAlert("p1", "", 200.0, "SORTIE_ZONE_SAFE");

        verify(notificationService, never()).notifySoignant(any(), any(), any());
    }

    // ── private helper ────────────────────────────────────────────────────────
    private NotificationPreference prefWith(boolean email, boolean voice) {
        NotificationPreference p = new NotificationPreference();
        p.setSoignantId("s1");
        p.setEmailEnabled(email);
        p.setVoiceEnabled(voice);
        return p;
    }
}