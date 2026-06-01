package com.esprit.campconnect.Reservation.Service;

import com.esprit.campconnect.Event.Entity.Event;
import com.esprit.campconnect.Reservation.DTO.UserNotificationResponseDTO;
import com.esprit.campconnect.Reservation.Entity.Reservation;
import com.esprit.campconnect.Reservation.Entity.UserNotification;
import com.esprit.campconnect.Reservation.Enum.NotificationType;
import com.esprit.campconnect.Reservation.Enum.PaymentStatus;
import com.esprit.campconnect.Reservation.Repository.UserNotificationRepository;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserNotificationServiceTest {

    private UserNotificationRepository userNotificationRepository;
    private UtilisateurRepository utilisateurRepository;
    private NotificationEmailService notificationEmailService;
    private UserNotificationService service;

    @BeforeEach
    void setUp() {
        userNotificationRepository = mock(UserNotificationRepository.class);
        utilisateurRepository = mock(UtilisateurRepository.class);
        notificationEmailService = mock(NotificationEmailService.class);
        service = new UserNotificationService(
                userNotificationRepository,
                utilisateurRepository,
                notificationEmailService
        );
    }

    @Test
    void notifyPaymentConfirmedCreatesNotificationAndSendsEmail() {
        Reservation reservation = paidReservation(false);
        when(userNotificationRepository.save(any(UserNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.notifyPaymentConfirmed(reservation);

        ArgumentCaptor<UserNotification> captor = ArgumentCaptor.forClass(UserNotification.class);
        verify(userNotificationRepository).save(captor.capture());
        UserNotification notification = captor.getValue();
        assertThat(notification.getUtilisateur()).isSameAs(reservation.getUtilisateur());
        assertThat(notification.getReservation()).isSameAs(reservation);
        assertThat(notification.getEvent()).isSameAs(reservation.getEvent());
        assertThat(notification.getType()).isEqualTo(NotificationType.PAYMENT_CONFIRMED);
        assertThat(notification.getTitle()).isEqualTo("Payment confirmed");
        assertThat(notification.getMessage()).contains("$120.5", "Forest Camp");
        assertThat(notification.getActionLabel()).isEqualTo("Open receipt");
        assertThat(notification.getActionUrl()).contains("focusReservation=11");
        assertThat(notification.getRead()).isFalse();
        verify(notificationEmailService).sendNotificationEmail(notification);
    }

    @Test
    void getNotificationsForUserMapsEntitiesToResponseDtos() {
        Utilisateur user = user();
        UserNotification notification = notification(22L, user, false);
        when(utilisateurRepository.findByEmail("camper@example.com")).thenReturn(Optional.of(user));
        when(userNotificationRepository.findByUtilisateurIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(notification));

        List<UserNotificationResponseDTO> notifications = service.getNotificationsForUser("camper@example.com");

        assertThat(notifications).hasSize(1);
        UserNotificationResponseDTO dto = notifications.get(0);
        assertThat(dto.getId()).isEqualTo(22L);
        assertThat(dto.getType()).isEqualTo(NotificationType.EVENT_REMINDER);
        assertThat(dto.getTitle()).isEqualTo("Event reminder");
        assertThat(dto.getReservationId()).isEqualTo(11L);
        assertThat(dto.getEventId()).isEqualTo(7L);
        assertThat(dto.getRead()).isFalse();
    }

    @Test
    void markAsReadUpdatesUnreadNotificationOnly() {
        Utilisateur user = user();
        UserNotification notification = notification(22L, user, false);
        when(utilisateurRepository.findByEmail("camper@example.com")).thenReturn(Optional.of(user));
        when(userNotificationRepository.findByIdAndUtilisateurId(22L, 5L)).thenReturn(Optional.of(notification));
        when(userNotificationRepository.save(notification)).thenReturn(notification);

        UserNotificationResponseDTO dto = service.markAsRead(22L, "camper@example.com");

        assertThat(notification.getRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
        assertThat(dto.getRead()).isTrue();
    }

    @Test
    void markAllAsReadUpdatesUnreadNotificationsAndKeepsReadTimestamp() {
        Utilisateur user = user();
        UserNotification unread = notification(22L, user, false);
        UserNotification alreadyRead = notification(23L, user, true);
        LocalDateTime existingReadAt = alreadyRead.getReadAt();
        when(utilisateurRepository.findByEmail("camper@example.com")).thenReturn(Optional.of(user));
        when(userNotificationRepository.findByUtilisateurIdOrderByCreatedAtDesc(5L))
                .thenReturn(List.of(unread, alreadyRead));

        service.markAllAsRead("camper@example.com");

        assertThat(unread.getRead()).isTrue();
        assertThat(unread.getReadAt()).isNotNull();
        assertThat(alreadyRead.getReadAt()).isEqualTo(existingReadAt);
        verify(userNotificationRepository).saveAll(List.of(unread, alreadyRead));
    }

    @Test
    void userLookupFailuresReturnHttpErrors() {
        assertThatThrownBy(() -> service.getUnreadCountForUser(" "))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authenticated user email is missing");

        when(utilisateurRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getUnreadCountForUser("missing@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authenticated user not found");
    }

    private Reservation paidReservation(boolean waitlist) {
        Utilisateur user = user();
        Event event = new Event();
        event.setId(7L);
        event.setTitre("Forest Camp");

        Reservation reservation = new Reservation();
        reservation.setId(11L);
        reservation.setUtilisateur(user);
        reservation.setEvent(event);
        reservation.setPrixTotal(new BigDecimal("120.50"));
        reservation.setStatutPaiement(PaymentStatus.PAID);
        reservation.setEstEnAttente(waitlist);
        return reservation;
    }

    private UserNotification notification(Long id, Utilisateur user, boolean read) {
        Reservation reservation = paidReservation(false);
        reservation.setUtilisateur(user);

        UserNotification notification = new UserNotification();
        notification.setId(id);
        notification.setUtilisateur(user);
        notification.setReservation(reservation);
        notification.setEvent(reservation.getEvent());
        notification.setType(NotificationType.EVENT_REMINDER);
        notification.setTitle("Event reminder");
        notification.setMessage("Forest Camp starts soon.");
        notification.setRead(read);
        notification.setReadAt(read ? LocalDateTime.now().minusDays(1) : null);
        notification.setActionLabel("Open reservation");
        notification.setActionUrl("/public/events/my-reservations?focusReservation=11");
        return notification;
    }

    private Utilisateur user() {
        Utilisateur user = new Utilisateur();
        user.setId(5L);
        user.setNom("Camper One");
        user.setEmail("camper@example.com");
        return user;
    }
}
