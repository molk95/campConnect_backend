package com.esprit.campconnect.Event.Service;

import com.esprit.campconnect.Event.Entity.Event;
import com.esprit.campconnect.Event.Enum.EventStatus;
import com.esprit.campconnect.Event.Repository.EventFavoriteRepository;
import com.esprit.campconnect.Event.Repository.EventImageRepository;
import com.esprit.campconnect.Event.Repository.EventRepository;
import com.esprit.campconnect.Reservation.Repository.ReservationRepository;
import com.esprit.campconnect.Reservation.Service.UserNotificationService;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import com.esprit.campconnect.config.GoogleMapsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private EventFavoriteRepository eventFavoriteRepository;
    @Mock
    private EventImageRepository eventImageRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserNotificationService userNotificationService;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private GoogleMapsService googleMapsService;

    private EventServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EventServiceImpl(
                eventRepository,
                utilisateurRepository,
                eventFavoriteRepository,
                eventImageRepository,
                reservationRepository,
                userNotificationService,
                fileStorageService,
                new ObjectMapper(),
                googleMapsService
        );
    }

    @Test
    void startEventMarksScheduledEventAsOngoing() {
        Event event = new Event();
        event.setId(42L);
        event.setStatut(EventStatus.SCHEDULED);
        when(eventRepository.findById(42L)).thenReturn(Optional.of(event));

        service.startEvent(42L);

        assertThat(event.getStatut()).isEqualTo(EventStatus.ONGOING);
        assertThat(event.getDateModification()).isNotNull();
        verify(eventRepository).save(event);
    }

    @Test
    void completeEventRejectsEventThatIsNotOngoing() {
        Event event = new Event();
        event.setId(42L);
        event.setStatut(EventStatus.SCHEDULED);
        when(eventRepository.findById(42L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.completeEvent(42L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only ongoing events can be completed");

        verify(eventRepository, never()).save(event);
    }

    @Test
    void addFavoriteDoesNothingWhenFavoriteAlreadyExists() {
        when(eventFavoriteRepository.existsByUtilisateurIdAndEventId(7L, 42L)).thenReturn(true);

        service.addFavorite(42L, 7L);

        verify(utilisateurRepository, never()).findById(7L);
        verify(eventRepository, never()).findById(42L);
        verify(eventFavoriteRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
