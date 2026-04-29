package tn.SoftCare.Geofencing.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import tn.SoftCare.Geofencing.Client.UserClient;
import tn.SoftCare.Geofencing.dto.UserDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires — SosService.
 * UserClient et NotificationService sont mockés : aucun appel réseau/mail/Twilio.
 */
class SosServiceTest {

    @InjectMocks
    private SosService sosService;

    @Mock private UserClient          userClient;
    @Mock private NotificationService notificationService;

    // ── Helper ────────────────────────────────────────────────────────────────
    // UserDto utilise firstName + lastName (pas fullName directement)
    // getFullName() retourne firstName + " " + lastName
    private UserDto userDto(String id, String firstName, String lastName,
                            String email, String numTel) {
        UserDto u = new UserDto();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setNumTel(numTel);
        return u;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ── Cas nominal ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleSos → appel vocal ET email envoyés si soignant a téléphone et email")
    void handleSos_sendsVoiceCallAndEmail_whenSoignantHasBoth() {
        UserDto patient  = userDto("p1", "Patient",   "Un",  "p1@test.com", "0612345678");
        UserDto soignant = userDto("s1", "Soignant",  "Un",  "s1@test.com", "0698765432");

        when(userClient.getUserById("p1")).thenReturn(patient);
        when(userClient.getUserById("s1")).thenReturn(soignant);

        sosService.handleSos("p1", "s1", 36.8, 10.2);

        // On vérifie le téléphone et les coordonnées — le nom est géré par UserDto.getFullName()
        verify(notificationService).sendSosVoiceCall(eq("0698765432"), anyString());
        verify(notificationService).sendSosEmail(
                eq("s1@test.com"), anyString(), eq("p1"), eq(36.8), eq(10.2));
    }

    // ── Soignant introuvable ──────────────────────────────────────────────────

    @Test
    @DisplayName("handleSos → aucune notification si soignant introuvable")
    void handleSos_skipsNotification_whenSoignantNotFound() {
        when(userClient.getUserById("p1")).thenReturn(
                userDto("p1", "Patient", "Un", "p1@test.com", "0612345678"));
        when(userClient.getUserById("s1")).thenReturn(null);

        sosService.handleSos("p1", "s1", 36.8, 10.2);

        verify(notificationService, never()).sendSosVoiceCall(any(), any());
        verify(notificationService, never()).sendSosEmail(any(), any(), any(), anyDouble(), anyDouble());
    }

    // ── Soignant sans téléphone ───────────────────────────────────────────────

    @Test
    @DisplayName("handleSos → email envoyé mais pas d'appel si soignant sans téléphone")
    void handleSos_skipsVoiceCall_whenSoignantHasNoPhone() {
        UserDto patient  = userDto("p1", "Patient",  "Un", "p1@test.com", "0612345678");
        UserDto soignant = userDto("s1", "Soignant", "Un", "s1@test.com", null);

        when(userClient.getUserById("p1")).thenReturn(patient);
        when(userClient.getUserById("s1")).thenReturn(soignant);

        sosService.handleSos("p1", "s1", 36.8, 10.2);

        verify(notificationService, never()).sendSosVoiceCall(any(), any());
        verify(notificationService).sendSosEmail(
                eq("s1@test.com"), anyString(), eq("p1"), eq(36.8), eq(10.2));
    }

    @Test
    @DisplayName("handleSos → email envoyé mais pas d'appel si téléphone soignant vide")
    void handleSos_skipsVoiceCall_whenSoignantPhoneIsEmpty() {
        UserDto patient  = userDto("p1", "Patient",  "Un", "p1@test.com", "0612345678");
        UserDto soignant = userDto("s1", "Soignant", "Un", "s1@test.com", "");

        when(userClient.getUserById("p1")).thenReturn(patient);
        when(userClient.getUserById("s1")).thenReturn(soignant);

        sosService.handleSos("p1", "s1", 36.8, 10.2);

        verify(notificationService, never()).sendSosVoiceCall(any(), any());
        verify(notificationService).sendSosEmail(any(), any(), any(), anyDouble(), anyDouble());
    }

    // ── Soignant sans email ───────────────────────────────────────────────────

    @Test
    @DisplayName("handleSos → appel envoyé mais pas d'email si soignant sans email")
    void handleSos_skipsEmail_whenSoignantHasNoEmail() {
        UserDto patient  = userDto("p1", "Patient",  "Un", "p1@test.com", "0612345678");
        UserDto soignant = userDto("s1", "Soignant", "Un", null, "0698765432");

        when(userClient.getUserById("p1")).thenReturn(patient);
        when(userClient.getUserById("s1")).thenReturn(soignant);

        sosService.handleSos("p1", "s1", 36.8, 10.2);

        verify(notificationService).sendSosVoiceCall(eq("0698765432"), anyString());
        verify(notificationService, never()).sendSosEmail(any(), any(), any(), anyDouble(), anyDouble());
    }

    // ── Patient introuvable ───────────────────────────────────────────────────

    @Test
    @DisplayName("handleSos → utilise patientId comme nom si patient introuvable")
    void handleSos_usesPatientIdAsName_whenPatientNotFound() {
        when(userClient.getUserById("p1")).thenReturn(null);
        when(userClient.getUserById("s1")).thenReturn(
                userDto("s1", "Soignant", "Un", "s1@test.com", "0698765432"));

        sosService.handleSos("p1", "s1", 36.8, 10.2);

        // fallback : patientName = patientId = "p1"
        verify(notificationService).sendSosVoiceCall(eq("0698765432"), eq("p1"));
        verify(notificationService).sendSosEmail(
                eq("s1@test.com"), eq("p1"), eq("p1"), eq(36.8), eq(10.2));
    }

    // ── UserClient lance une exception ────────────────────────────────────────

    @Test
    @DisplayName("handleSos → aucune exception propagée si UserClient échoue")
    void handleSos_doesNotThrow_whenUserClientFails() {
        when(userClient.getUserById(any())).thenThrow(new RuntimeException("service down"));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> sosService.handleSos("p1", "s1", 36.8, 10.2)
        );

        verify(notificationService, never()).sendSosVoiceCall(any(), any());
        verify(notificationService, never()).sendSosEmail(any(), any(), any(), anyDouble(), anyDouble());
    }

    // ── Coordonnées GPS correctement transmises ───────────────────────────────

    @Test
    @DisplayName("handleSos → coordonnées GPS transmises telles quelles à sendSosEmail")
    void handleSos_passesCorrectCoordinatesToEmail() {
        UserDto patient  = userDto("p1", "Patient",  "Un", "p1@test.com", "0612345678");
        UserDto soignant = userDto("s1", "Soignant", "Un", "s1@test.com", "0698765432");

        when(userClient.getUserById("p1")).thenReturn(patient);
        when(userClient.getUserById("s1")).thenReturn(soignant);

        sosService.handleSos("p1", "s1", 33.887, 9.537);

        verify(notificationService).sendSosEmail(
                any(), any(), any(), eq(33.887), eq(9.537));
    }
}