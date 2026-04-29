package tn.SoftCare.Tracking.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.SoftCare.Tracking.Entity.Position;
import tn.SoftCare.Tracking.Service.TrackingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrackingControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private TrackingService trackingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TrackingController controller = new TrackingController();
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "service", trackingService);

        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(converter)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/tracking/add
    // ─────────────────────────────────────────────────────────────

    @Test
    void ajouterPosition_returnsSavedPosition() throws Exception {
        Position saved = new Position(1L, "p1", 36.8, 10.2, LocalDateTime.now());
        when(trackingService.saveAndProcess(any())).thenReturn(saved);

        mockMvc.perform(post("/api/tracking/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {"patientId":"p1","latitude":36.8,"longitude":10.2,"accuracy":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value("p1"))
                // ✅ AJOUTÉ : vérification des coordonnées
                .andExpect(jsonPath("$.latitude").value(36.8))
                .andExpect(jsonPath("$.longitude").value(10.2));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/tracking/last
    // ─────────────────────────────────────────────────────────────

    @Test
    void getAllLastPositions_returnsList() throws Exception {
        when(trackingService.getAllLastPositions()).thenReturn(List.of(
                new Position(1L, "p1", 36.8, 10.2, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/tracking/last")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value("p1"))
                // ✅ AJOUTÉ : vérification de la taille de la liste
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllLastPositions_whenEmpty_returnsEmptyList() throws Exception {
        // ✅ AJOUTÉ : cas où aucun patient n'a de position
        when(trackingService.getAllLastPositions()).thenReturn(List.of());

        mockMvc.perform(get("/api/tracking/last")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/tracking/last/{patientId}
    // ─────────────────────────────────────────────────────────────

    // ✅ AJOUTÉ : cas où le patient existe
    @Test
    void getLastPosition_whenFound_returnsPosition() throws Exception {
        Position pos = new Position(1L, "p1", 36.8, 10.2, LocalDateTime.now());
        when(trackingService.getLastPosition(eq("p1"))).thenReturn(pos);

        mockMvc.perform(get("/api/tracking/last/p1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value("p1"))
                .andExpect(jsonPath("$.latitude").value(36.8))
                .andExpect(jsonPath("$.longitude").value(10.2));
    }

    @Test
    void getLastPosition_whenMissing_returnsEmptyBody() throws Exception {
        when(trackingService.getLastPosition(eq("missing"))).thenReturn(null);

        mockMvc.perform(get("/api/tracking/last/missing")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }
}