package tn.SoftCare.Geofencing.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.SoftCare.Geofencing.Entity.Alert;
import tn.SoftCare.Geofencing.Service.AlertService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires — AlertController.
 */
class AlertControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AlertService alertService;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AlertController controller = new AlertController();
        ReflectionTestUtils.setField(controller, "alertService", alertService);

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }

    // ── GET /api/geofencing/alerts ────────────────────────────────────────────

    @Test
    @DisplayName("GET /alerts → retourne toutes les alertes")
    void getAll_returnsAllAlerts() throws Exception {
        when(alertService.getAllAlerts()).thenReturn(List.of(
                alert(1L, "patient-1", "soignant-1", "Active"),
                alert(2L, "patient-2", "soignant-1", "Resolved")
        ));

        mockMvc.perform(get("/api/geofencing/alerts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].patientId").value("patient-1"))
                .andExpect(jsonPath("$[1].status").value("Resolved"));
    }

    @Test
    @DisplayName("GET /alerts → liste vide si aucune alerte")
    void getAll_returnsEmpty_whenNone() throws Exception {
        when(alertService.getAllAlerts()).thenReturn(List.of());

        mockMvc.perform(get("/api/geofencing/alerts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/geofencing/alerts/patient/{patientId} ────────────────────────

    @Test
    @DisplayName("GET /alerts/patient/{id} → alertes du patient")
    void getByPatient_returnsAlerts() throws Exception {
        when(alertService.getAlertsByPatient("patient-1"))
                .thenReturn(List.of(alert(1L, "patient-1", "soignant-1", "Active")));

        mockMvc.perform(get("/api/geofencing/alerts/patient/patient-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].patientId").value("patient-1"))
                .andExpect(jsonPath("$[0].status").value("Active"));
    }

    @Test
    @DisplayName("GET /alerts/patient/{id} → vide si patient sans alertes")
    void getByPatient_returnsEmpty() throws Exception {
        when(alertService.getAlertsByPatient("patient-99")).thenReturn(List.of());

        mockMvc.perform(get("/api/geofencing/alerts/patient/patient-99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/geofencing/alerts/soignant/{soignantId} ─────────────────────

    @Test
    @DisplayName("GET /alerts/soignant/{id} → alertes supervisées par le soignant")
    void getBySoignant_returnsAlerts() throws Exception {
        when(alertService.getAlertsBySoignant("soignant-1")).thenReturn(List.of(
                alert(1L, "patient-1", "soignant-1", "Active"),
                alert(2L, "patient-2", "soignant-1", "Active")
        ));

        mockMvc.perform(get("/api/geofencing/alerts/soignant/soignant-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].soignantId").value("soignant-1"));
    }

    // ── PUT /api/geofencing/alerts/{id}/resolve ───────────────────────────────

    @Test
    @DisplayName("PUT /alerts/{id}/resolve → retourne l'alerte avec status Resolved")
    void resolve_returnsResolvedAlert() throws Exception {
        Alert resolved = alert(1L, "patient-1", "soignant-1", "Resolved");
        when(alertService.resolveAlert(1L)).thenReturn(resolved);

        mockMvc.perform(put("/api/geofencing/alerts/1/resolve")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("Resolved"));

        verify(alertService, times(1)).resolveAlert(1L);
    }

    @Test
    @DisplayName("PUT /alerts/{id}/resolve → service appelé avec le bon id")
    void resolve_callsServiceWithCorrectId() throws Exception {
        Alert resolved = alert(42L, "patient-5", "soignant-2", "Resolved");
        when(alertService.resolveAlert(42L)).thenReturn(resolved);

        mockMvc.perform(put("/api/geofencing/alerts/42/resolve")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42));

        verify(alertService, times(1)).resolveAlert(42L);
        verify(alertService, never()).resolveAlert(1L);
    }
}