package com.alzheimer.event_service.services;

import com.alzheimer.event_service.dto.EventCreateRequest;
import com.alzheimer.event_service.dto.EventResponse;
import com.alzheimer.event_service.entities.Event;
import com.alzheimer.event_service.repositories.EventParticipationRepository;
import com.alzheimer.event_service.repositories.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventParticipationRepository participationRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private EventService eventService;

    @Test
    void create_whenReminderEnabled_callsEmailAndReturnsResponse() {
        EventCreateRequest request = new EventCreateRequest();
        request.setTitle("Memory Workshop");
        request.setDescription("desc");
        request.setStartDateTime(LocalDateTime.now().plusDays(1));
        request.setLocation("Tunis");
        request.setRemindEnabled(true);
        request.setUserId(1L);

        Event saved = new Event();
        saved.setId(5L);
        saved.setTitle("Memory Workshop");
        saved.setDescription("desc");
        saved.setStartDateTime(request.getStartDateTime());
        saved.setLocation("Tunis");
        saved.setRemindEnabled(true);
        saved.setUserId(1L);

        when(eventRepository.save(any(Event.class))).thenReturn(saved);

        EventResponse result = eventService.create(request);

        assertEquals(5L, result.getId());
        verify(emailService).sendImmediateReminderToStaticUser(saved);
    }

    @Test
    void getById_whenMissing_throwsRuntimeException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> eventService.getById(99L));

        assertEquals("Event not found", ex.getMessage());
    }

    @Test
    void delete_shouldDeleteParticipationsThenEvent() {
        eventService.delete(7L);

        verify(participationRepository).deleteAllByEvent_Id(7L);
        verify(eventRepository).deleteById(7L);
        verify(eventRepository, never()).findById(7L);
    }
}
