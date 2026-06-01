package com.esprit.campconnect.Reservation.Service;

import com.esprit.campconnect.Event.Entity.Event;
import com.esprit.campconnect.Reservation.Entity.Reservation;
import com.esprit.campconnect.Reservation.Enum.ReservationStatus;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.config.GoogleMapsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationCalendarServiceTest {

    private GoogleMapsService googleMapsService;
    private ReservationCalendarService service;

    @BeforeEach
    void setUp() {
        googleMapsService = mock(GoogleMapsService.class);
        service = new ReservationCalendarService(googleMapsService);
    }

    @Test
    void isCalendarExportAvailableRejectsMissingFinishedOrCancelledReservations() {
        assertThat(service.isCalendarExportAvailable(null)).isFalse();
        assertThat(service.isCalendarExportAvailable(new Reservation())).isFalse();

        Reservation cancelled = reservation(ReservationStatus.CANCELLED, false);
        assertThat(service.isCalendarExportAvailable(cancelled)).isFalse();

        Reservation paid = reservation(ReservationStatus.PAID, false);
        assertThat(service.isCalendarExportAvailable(paid)).isTrue();
    }

    @Test
    void buildGoogleCalendarUrlEncodesEventDetailsAndWaitlistTitle() {
        Reservation reservation = reservation(ReservationStatus.PENDING, true);
        when(googleMapsService.buildGoogleMapsUrl(reservation.getEvent()))
                .thenReturn("https://maps.example/camp");

        String url = service.buildGoogleCalendarUrl(reservation);

        assertThat(url)
                .startsWith("https://calendar.google.com/calendar/render?action=TEMPLATE")
                .contains(
                        "text=Forest%20Camp%20(Waitlist)",
                        "dates=20260710T140000/20260710T163000",
                        "ctz=Africa/Lagos",
                        "location=Lake%20Point%20Camp",
                        "Maps:%20https://maps.example/camp"
                );
    }

    @Test
    void generateIcsInviteEscapesTextIncludesOrganizerAndTentativeWaitlistStatus() {
        Reservation reservation = reservation(ReservationStatus.PENDING, true);
        reservation.setRemarques("Need vegetarian food, no nuts; thanks");
        when(googleMapsService.buildGoogleMapsUrl(reservation.getEvent()))
                .thenReturn("https://maps.example/camp");

        String invite = new String(service.generateIcsInvite(reservation), StandardCharsets.UTF_8);

        assertThat(invite)
                .contains(
                        "BEGIN:VCALENDAR",
                        "UID:reservation-42@campconnect.local",
                        "DTSTART:20260710T130000Z",
                        "SUMMARY:Forest Camp (Waitlist)",
                        "STATUS:TENTATIVE",
                        "LOCATION:Lake Point Camp",
                        "ORGANIZER;CN=Organizer One:MAILTO:organizer@example.com"
                );
        assertThat(invite.replace("\r\n ", "")).contains("Booking notes: Need vegetarian food\\, no nuts\\; thanks");
    }

    @Test
    void buildsFallbackDownloadUrlAndStableSuggestedFilename() {
        Reservation reservation = reservation(ReservationStatus.CONFIRMED, false);
        reservation.getEvent().setTitre("Camp & Kayak Weekend!");

        assertThat(service.buildIcsDownloadUrl(null)).isNull();
        assertThat(service.buildIcsDownloadUrl(99L)).isEqualTo("/api/reservations/99/calendar.ics");
        assertThat(service.buildSuggestedFilename(null)).isEqualTo("campconnect-reservation.ics");
        assertThat(service.buildSuggestedFilename(reservation)).isEqualTo("reservation-42-camp-kayak-weekend.ics");
    }

    private Reservation reservation(ReservationStatus status, boolean waitlist) {
        Utilisateur organizer = new Utilisateur();
        organizer.setNom("Organizer One");
        organizer.setEmail("organizer@example.com");

        Event event = new Event();
        event.setId(7L);
        event.setTitre("Forest Camp");
        event.setDescription("Bring water and warm clothes.");
        event.setLieu("Lake Point Camp");
        event.setDateDebut(LocalDateTime.of(2026, 7, 10, 14, 0));
        event.setDateFin(LocalDateTime.of(2026, 7, 10, 16, 30));
        event.setDureeMinutes(150);
        event.setOrganizer(organizer);

        Reservation reservation = new Reservation();
        reservation.setId(42L);
        reservation.setEvent(event);
        reservation.setStatut(status);
        reservation.setNombreParticipants(2);
        reservation.setEstEnAttente(waitlist);
        return reservation;
    }
}
