package tn.SoftCare.Tracking.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TrackingService trackingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TrackingController controller = new TrackingController();
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "service", trackingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void ajouterPosition_returnsSavedPosition() throws Exception {
        Position saved = new Position(1L, "p1", 36.8, 10.2, LocalDateTime.now());
        when(trackingService.saveAndProcess(any())).thenReturn(saved);

        mockMvc.perform(post("/api/tracking/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"patientId":"p1","latitude":36.8,"longitude":10.2,"accuracy":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value("p1"));
    }

    @Test
    void getAllLastPositions_returnsList() throws Exception {
        when(trackingService.getAllLastPositions()).thenReturn(List.of(
                new Position(1L, "p1", 36.8, 10.2, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/tracking/last"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value("p1"));
    }

    @Test
    void getLastPosition_whenMissing_returnsEmptyBody() throws Exception {
        when(trackingService.getLastPosition(eq("missing"))).thenReturn(null);

        mockMvc.perform(get("/api/tracking/last/missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }
}
