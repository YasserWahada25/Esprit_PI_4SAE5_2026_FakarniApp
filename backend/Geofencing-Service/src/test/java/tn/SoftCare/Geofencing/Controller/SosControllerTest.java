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
import tn.SoftCare.Geofencing.Service.SosService;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires — SosController.
 */
class SosControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SosService sosService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SosController controller = new SosController();
        ReflectionTestUtils.setField(controller, "sosService", sosService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    // ── POST /api/geofencing/sos/trigger ─────────────────────────────────────

    @Test
    @DisplayName("POST /sos/trigger → 200 OK avec message de confirmation")
    void triggerSos_returns200WithStatus() throws Exception {
        doNothing().when(sosService)
                .handleSos(anyString(), anyString(), anyDouble(), anyDouble());

        Map<String, Object> payload = Map.of(
                "patientId",  "patient-1",
                "soignantId", "soignant-1",
                "latitude",   36.8,
                "longitude",  10.2
        );

        mockMvc.perform(post("/api/geofencing/sos/trigger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOS envoyé avec succès"));

        verify(sosService).handleSos("patient-1", "soignant-1", 36.8, 10.2);
    }

    @Test
    @DisplayName("POST /sos/trigger → service appelé avec les bonnes coordonnées GPS")
    void triggerSos_passesCorrectCoordinates() throws Exception {
        doNothing().when(sosService)
                .handleSos(anyString(), anyString(), anyDouble(), anyDouble());

        Map<String, Object> payload = Map.of(
                "patientId",  "patient-99",
                "soignantId", "soignant-42",
                "latitude",   33.887,
                "longitude",  9.537
        );

        mockMvc.perform(post("/api/geofencing/sos/trigger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(sosService).handleSos("patient-99", "soignant-42", 33.887, 9.537);
    }
}