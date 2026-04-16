package tn.SoftCare.Tracking.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.SoftCare.Tracking.Client.GeofencingClient;
import tn.SoftCare.Tracking.Entity.Position;
import tn.SoftCare.Tracking.Repository.TrackingRepository;
import tn.SoftCare.Tracking.dto.PositionRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock
    private TrackingRepository repository;
    @Mock
    private GeofencingClient geofencingClient;

    @InjectMocks
    private TrackingService trackingService;

    private PositionRequest request;

    @BeforeEach
    void setUp() {
        request = new PositionRequest();
        request.setPatientId("patient-1");
        request.setLatitude(36.8);
        request.setLongitude(10.2);
    }

    @Test
    void saveAndProcess_success_callsRepositoryAndGeofencing() {
        Position saved = new Position();
        saved.setPatientId("patient-1");
        saved.setLatitude(36.8);
        saved.setLongitude(10.2);
        when(repository.save(any(Position.class))).thenReturn(saved);

        Position result = trackingService.saveAndProcess(request);

        assertEquals("patient-1", result.getPatientId());
        verify(repository, times(1)).save(any(Position.class));
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(geofencingClient).envoyerPourAnalyse(payloadCaptor.capture());
        assertEquals("patient-1", payloadCaptor.getValue().get("patientId"));
    }

    @Test
    void saveAndProcess_whenGeofencingFails_stillReturnsSavedPosition() {
        Position saved = new Position();
        saved.setPatientId("patient-1");
        saved.setLatitude(36.8);
        saved.setLongitude(10.2);
        when(repository.save(any(Position.class))).thenReturn(saved);
        doThrow(new RuntimeException("downstream unavailable"))
                .when(geofencingClient).envoyerPourAnalyse(any());

        Position result = trackingService.saveAndProcess(request);

        assertNotNull(result);
        assertEquals("patient-1", result.getPatientId());
        verify(repository).save(any(Position.class));
    }

    @Test
    void getLastPosition_whenNotFound_returnsNull() {
        when(repository.findTopByPatientIdOrderByTimestampDesc("missing")).thenReturn(Optional.empty());

        Position result = trackingService.getLastPosition("missing");

        assertNull(result);
    }

    @Test
    void getAllLastPositions_returnsRepositoryData() {
        when(repository.findAllLastPositions()).thenReturn(List.of(new Position()));

        List<Position> result = trackingService.getAllLastPositions();

        assertEquals(1, result.size());
    }
}
