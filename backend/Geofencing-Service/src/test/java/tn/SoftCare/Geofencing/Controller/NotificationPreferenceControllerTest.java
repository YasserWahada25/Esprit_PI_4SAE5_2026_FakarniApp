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
import tn.SoftCare.Geofencing.Entity.NotificationPreference;
import tn.SoftCare.Geofencing.Repository.NotificationPreferenceRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires — NotificationPreferenceController.
 */
class NotificationPreferenceControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private NotificationPreferenceRepository prefRepository;

    // ── Helper ────────────────────────────────────────────────────────────────
    private NotificationPreference pref(Long id, String soignantId,
                                        boolean email, boolean voice) {
        NotificationPreference p = new NotificationPreference();
        p.setId(id);
        p.setSoignantId(soignantId);
        p.setEmailEnabled(email);
        p.setVoiceEnabled(voice);
        return p;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        NotificationPreferenceController controller = new NotificationPreferenceController();
        ReflectionTestUtils.setField(controller, "prefRepository", prefRepository);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    // ── GET /api/geofencing/preferences/{soignantId} ──────────────────────────

    @Test
    @DisplayName("GET /preferences/{id} → retourne les préférences existantes")
    void getPreferences_returnsExistingPrefs() throws Exception {
        when(prefRepository.findBySoignantId("soignant-1"))
                .thenReturn(Optional.of(pref(1L, "soignant-1", true, false)));

        mockMvc.perform(get("/api/geofencing/preferences/soignant-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.soignantId").value("soignant-1"))
                .andExpect(jsonPath("$.emailEnabled").value(true))
                .andExpect(jsonPath("$.voiceEnabled").value(false));
    }

    @Test
    @DisplayName("GET /preferences/{id} → préférences par défaut si soignant inconnu")
    void getPreferences_returnsDefaults_whenNotFound() throws Exception {
        when(prefRepository.findBySoignantId("nouveau")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/geofencing/preferences/nouveau")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Par défaut : email ON, voice OFF (logique du contrôleur)
                .andExpect(jsonPath("$.emailEnabled").value(true))
                .andExpect(jsonPath("$.voiceEnabled").value(false));
    }

    // ── POST /api/geofencing/preferences ─────────────────────────────────────

    @Test
    @DisplayName("POST /preferences → met à jour les préférences existantes")
    void savePreferences_updatesExisting() throws Exception {
        NotificationPreference existing = pref(1L, "soignant-1", true, false);
        NotificationPreference updated  = pref(1L, "soignant-1", false, true);

        when(prefRepository.findBySoignantId("soignant-1")).thenReturn(Optional.of(existing));
        when(prefRepository.save(any(NotificationPreference.class))).thenReturn(updated);

        mockMvc.perform(post("/api/geofencing/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailEnabled").value(false))
                .andExpect(jsonPath("$.voiceEnabled").value(true));

        verify(prefRepository).save(any(NotificationPreference.class));
    }

    @Test
    @DisplayName("POST /preferences → crée de nouvelles préférences si soignant inconnu")
    void savePreferences_createsNew_whenNotFound() throws Exception {
        NotificationPreference newPref = pref(null, "nouveau", true, true);

        when(prefRepository.findBySoignantId("nouveau")).thenReturn(Optional.empty());
        when(prefRepository.save(any(NotificationPreference.class))).thenReturn(
                pref(10L, "nouveau", true, true));

        mockMvc.perform(post("/api/geofencing/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPref)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.soignantId").value("nouveau"))
                .andExpect(jsonPath("$.emailEnabled").value(true))
                .andExpect(jsonPath("$.voiceEnabled").value(true));
    }
}