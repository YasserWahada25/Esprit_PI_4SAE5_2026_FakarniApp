package tn.SoftCare.Geofencing.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import tn.SoftCare.Geofencing.Client.TrackingClient;
import tn.SoftCare.Geofencing.Entity.Zone;
import tn.SoftCare.Geofencing.Entity.ZoneType;
import tn.SoftCare.Geofencing.Repository.ZoneRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires — GeofencingService.
 * Toutes les dépendances externes sont mockées.
 */
class GeofencingServiceTest {

    @InjectMocks
    private GeofencingService geofencingService;

    @Mock private ZoneRepository  zoneRepository;
    @Mock private AlertService    alertService;
    @Mock private TrackingClient  trackingClient;

    // ── Helper ────────────────────────────────────────────────────────────────
    private Zone zone(Long id, String patientId, String soignantId,
                      double lat, double lon, double rayon, ZoneType type) {
        Zone z = new Zone();
        z.setId(id);
        z.setPatientId(patientId);
        z.setSoignantId(soignantId);
        z.setNomZone("Zone Test");
        z.setCentreLat(lat);
        z.setCentreLon(lon);
        z.setRayon(rayon);
        z.setType(type);
        return z;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ── addZone ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addZone → sauvegarde et retourne la zone")
    void addZone_savesAndReturns() {
        Zone input = zone(null, "p1", "s1", 36.8, 10.2, 100.0, ZoneType.SAFE);
        Zone saved  = zone(1L,  "p1", "s1", 36.8, 10.2, 100.0, ZoneType.SAFE);
        when(zoneRepository.save(input)).thenReturn(saved);

        Zone result = geofencingService.addZone(input);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPatientId()).isEqualTo("p1");
        assertThat(result.getType()).isEqualTo(ZoneType.SAFE);
        verify(zoneRepository).save(input);
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAll → retourne toutes les zones")
    void getAll_returnsAllZones() {
        when(zoneRepository.findAll()).thenReturn(List.of(
                zone(1L, "p1", "s1", 36.8, 10.2, 100.0, ZoneType.SAFE),
                zone(2L, "p2", "s1", 36.9, 10.3, 200.0, ZoneType.DANGER)
        ));

        List<Zone> result = geofencingService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(1).getType()).isEqualTo(ZoneType.DANGER);
    }

    // ── getZonesByPatient ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getZonesByPatient → filtre par patientId")
    void getZonesByPatient_returnsMatchingZones() {
        when(zoneRepository.findAllByPatientId("p1"))
                .thenReturn(List.of(zone(1L, "p1", "s1", 36.8, 10.2, 100.0, ZoneType.SAFE)));

        List<Zone> result = geofencingService.getZonesByPatient("p1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo("p1");
    }

    @Test
    @DisplayName("getZonesByPatient → liste vide si patient inconnu")
    void getZonesByPatient_returnsEmpty() {
        when(zoneRepository.findAllByPatientId("inconnu")).thenReturn(List.of());

        assertThat(geofencingService.getZonesByPatient("inconnu")).isEmpty();
    }

    // ── getZonesBySoignant ────────────────────────────────────────────────────

    @Test
    @DisplayName("getZonesBySoignant → filtre par soignantId")
    void getZonesBySoignant_returnsMatchingZones() {
        when(zoneRepository.findBySoignantId("s1")).thenReturn(List.of(
                zone(1L, "p1", "s1", 36.8, 10.2, 100.0, ZoneType.SAFE),
                zone(2L, "p2", "s1", 36.9, 10.3, 150.0, ZoneType.DANGER)
        ));

        List<Zone> result = geofencingService.getZonesBySoignant("s1");

        assertThat(result).hasSize(2);
        result.forEach(z -> assertThat(z.getSoignantId()).isEqualTo("s1"));
    }

    // ── updateZone ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateZone → modifie et retourne la zone mise à jour")
    void updateZone_updatesFields() {
        Zone existing = zone(1L, "p1", "s1", 36.8, 10.2, 100.0, ZoneType.SAFE);
        Zone patch    = zone(null, "p1", "s1", 36.85, 10.25, 200.0, ZoneType.SAFE);
        patch.setNomZone("Nouvelle Zone");

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(zoneRepository.save(any(Zone.class))).thenAnswer(i -> i.getArgument(0));

        Zone result = geofencingService.updateZone(1L, patch);

        assertThat(result.getRayon()).isEqualTo(200.0);
        assertThat(result.getNomZone()).isEqualTo("Nouvelle Zone");
        assertThat(result.getCentreLat()).isEqualTo(36.85);
        verify(zoneRepository).save(any(Zone.class));
    }

    @Test
    @DisplayName("updateZone → retourne null si zone introuvable")
    void updateZone_returnsNull_whenNotFound() {
        when(zoneRepository.findById(99L)).thenReturn(Optional.empty());

        Zone result = geofencingService.updateZone(99L, new Zone());

        assertThat(result).isNull();
        verify(zoneRepository, never()).save(any());
    }

    // ── deleteZone ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteZone → appelle deleteById avec le bon id")
    void deleteZone_callsRepository() {
        doNothing().when(zoneRepository).deleteById(1L);

        geofencingService.deleteZone(1L);

        verify(zoneRepository, times(1)).deleteById(1L);
    }

    // ── verifierPosition — Zone SAFE ──────────────────────────────────────────

    @Test
    @DisplayName("verifierPosition SAFE → createAlert si patient HORS zone SAFE")
    void verifierPosition_safe_alertWhenOutside() {
        // Zone SAFE centre (36.8, 10.2) rayon 50m
        // Patient à ~11km → hors zone → alerte SORTIE_ZONE_SAFE
        Zone z = zone(1L, "p1", "s1", 36.8, 10.2, 50.0, ZoneType.SAFE);
        when(zoneRepository.findByPatientId("p1")).thenReturn(Optional.of(z));

        geofencingService.verifierPosition("p1", 36.9, 10.3);

        verify(alertService).createAlert(eq("p1"), eq("s1"), anyDouble(), eq("SORTIE_ZONE_SAFE"));
    }

    @Test
    @DisplayName("verifierPosition SAFE → aucune alerte si patient DANS la zone SAFE")
    void verifierPosition_safe_noAlertWhenInside() {
        // Zone SAFE rayon 5000m, patient au centre → dedans
        Zone z = zone(1L, "p1", "s1", 36.8, 10.2, 5000.0, ZoneType.SAFE);
        when(zoneRepository.findByPatientId("p1")).thenReturn(Optional.of(z));

        geofencingService.verifierPosition("p1", 36.8, 10.2);

        verify(alertService, never()).createAlert(any(), any(), anyDouble(), any());
    }

    // ── verifierPosition — Zone DANGER ────────────────────────────────────────

    @Test
    @DisplayName("verifierPosition DANGER → createAlert si patient ENTRE dans zone DANGER")
    void verifierPosition_danger_alertWhenInside() {
        // Zone DANGER rayon 5000m, patient au centre exact → dedans → alerte
        Zone z = zone(1L, "p1", "s1", 36.8, 10.2, 5000.0, ZoneType.DANGER);
        when(zoneRepository.findByPatientId("p1")).thenReturn(Optional.of(z));

        geofencingService.verifierPosition("p1", 36.8, 10.2);

        verify(alertService).createAlert(eq("p1"), eq("s1"), anyDouble(), eq("ENTREE_ZONE_DANGER"));
    }

    @Test
    @DisplayName("verifierPosition DANGER → aucune alerte si patient HORS zone DANGER")
    void verifierPosition_danger_noAlertWhenOutside() {
        // Zone DANGER rayon 50m, patient à 11km → hors zone → pas d'alerte
        Zone z = zone(1L, "p1", "s1", 36.8, 10.2, 50.0, ZoneType.DANGER);
        when(zoneRepository.findByPatientId("p1")).thenReturn(Optional.of(z));

        geofencingService.verifierPosition("p1", 36.9, 10.3);

        verify(alertService, never()).createAlert(any(), any(), anyDouble(), any());
    }

    // ── verifierPosition — pas de zone ───────────────────────────────────────

    @Test
    @DisplayName("verifierPosition → aucune action si aucune zone définie pour ce patient")
    void verifierPosition_noAction_whenNoZone() {
        when(zoneRepository.findByPatientId("p1")).thenReturn(Optional.empty());

        geofencingService.verifierPosition("p1", 36.8, 10.2);

        verify(alertService, never()).createAlert(any(), any(), anyDouble(), any());
    }
}