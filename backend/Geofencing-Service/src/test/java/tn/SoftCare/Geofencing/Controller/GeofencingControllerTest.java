package tn.SoftCare.Geofencing.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import tn.SoftCare.Geofencing.Entity.Zone;
import tn.SoftCare.Geofencing.Entity.ZoneType;
import tn.SoftCare.Geofencing.Service.GeofencingService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires — GeofencingController.
 * Pas de contexte Spring : MockMvc standalone + Mockito.
 */
class GeofencingControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GeofencingService geofencingService;

    // ── Helper ────────────────────────────────────────────────────────────────
    private Zone zone(Long id, String patientId, String soignantId, ZoneType type) {
        Zone z = new Zone();
        z.setId(id);
        z.setPatientId(patientId);
        z.setSoignantId(soignantId);
        z.setNomZone("Zone Test");
        z.setCentreLat(36.8);
        z.setCentreLon(10.2);
        z.setRayon(100.0);
        z.setType(type);
        return z;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        GeofencingController controller = new GeofencingController();
        ReflectionTestUtils.setField(controller, "geofencingService", geofencingService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    // ── POST /api/geofencing/zone ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /zone → retourne la zone créée avec patientId et soignantId")
    void createZone_returnsCreatedZone() throws Exception {
        Zone z = zone(1L, "patient-1", "soignant-1", ZoneType.SAFE);
        when(geofencingService.addZone(any(Zone.class))).thenReturn(z);

        mockMvc.perform(post("/api/geofencing/zone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(z)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value("patient-1"))
                .andExpect(jsonPath("$.soignantId").value("soignant-1"))
                .andExpect(jsonPath("$.rayon").value(100.0))
                .andExpect(jsonPath("$.type").value("SAFE"));

        verify(geofencingService).addZone(any(Zone.class));
    }

    // ── GET /api/geofencing/zones ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /zones → retourne la liste complète")
    void listAll_returnsAllZones() throws Exception {
        when(geofencingService.getAll()).thenReturn(List.of(
                zone(1L, "patient-1", "soignant-1", ZoneType.SAFE),
                zone(2L, "patient-2", "soignant-1", ZoneType.DANGER)
        ));

        mockMvc.perform(get("/api/geofencing/zones").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].patientId").value("patient-1"))
                .andExpect(jsonPath("$[1].type").value("DANGER"));
    }

    @Test
    @DisplayName("GET /zones → liste vide si aucune zone")
    void listAll_returnsEmptyList() throws Exception {
        when(geofencingService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/geofencing/zones").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/geofencing/zones/patient/{patientId} ─────────────────────────

    @Test
    @DisplayName("GET /zones/patient/{id} → retourne les zones du patient")
    void listByPatient_returnsZones() throws Exception {
        when(geofencingService.getZonesByPatient("patient-1"))
                .thenReturn(List.of(zone(1L, "patient-1", "soignant-1", ZoneType.SAFE)));

        mockMvc.perform(get("/api/geofencing/zones/patient/patient-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].patientId").value("patient-1"));
    }

    @Test
    @DisplayName("GET /zones/patient/{id} → liste vide si patient inconnu")
    void listByPatient_returnsEmpty_whenUnknown() throws Exception {
        when(geofencingService.getZonesByPatient("inconnu")).thenReturn(List.of());

        mockMvc.perform(get("/api/geofencing/zones/patient/inconnu")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/geofencing/zones/soignant/{soignantId} ──────────────────────

    @Test
    @DisplayName("GET /zones/soignant/{id} → retourne les zones du soignant")
    void listBySoignant_returnsZones() throws Exception {
        when(geofencingService.getZonesBySoignant("soignant-1")).thenReturn(List.of(
                zone(1L, "patient-1", "soignant-1", ZoneType.SAFE),
                zone(2L, "patient-2", "soignant-1", ZoneType.DANGER)
        ));

        mockMvc.perform(get("/api/geofencing/zones/soignant/soignant-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].soignantId").value("soignant-1"));
    }

    // ── PUT /api/geofencing/zone/{id} ─────────────────────────────────────────

    @Test
    @DisplayName("PUT /zone/{id} → retourne la zone mise à jour")
    void updateZone_returnsUpdated() throws Exception {
        Zone updated = zone(1L, "patient-1", "soignant-1", ZoneType.SAFE);
        updated.setRayon(250.0);
        when(geofencingService.updateZone(eq(1L), any(Zone.class))).thenReturn(updated);

        mockMvc.perform(put("/api/geofencing/zone/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rayon").value(250.0));

        verify(geofencingService).updateZone(eq(1L), any(Zone.class));
    }

    // ── DELETE /api/geofencing/zone/{id} ─────────────────────────────────────

    @Test
    @DisplayName("DELETE /zone/{id} → 200 OK, service appelé une fois")
    void deleteZone_returns200() throws Exception {
        doNothing().when(geofencingService).deleteZone(1L);

        mockMvc.perform(delete("/api/geofencing/zone/1"))
                .andExpect(status().isOk());

        verify(geofencingService, times(1)).deleteZone(1L);
    }

    // ── POST /api/geofencing/analyser ─────────────────────────────────────────

    @Test
    @DisplayName("POST /analyser → appelle verifierPosition avec les bonnes valeurs")
    void analyser_delegatesToService() throws Exception {
        doNothing().when(geofencingService)
                .verifierPosition(anyString(), anyDouble(), anyDouble());

        Map<String, Object> payload = Map.of(
                "patientId", "patient-1",
                "latitude", 36.8,
                "longitude", 10.2
        );

        mockMvc.perform(post("/api/geofencing/analyser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(geofencingService).verifierPosition("patient-1", 36.8, 10.2);
    }
}