package com.esprit.campconnect.Notification.Service;

import com.esprit.campconnect.Notification.DTO.UserNotificationResponseDTO;
import com.esprit.campconnect.Notification.Entity.UserNotification;
import com.esprit.campconnect.Notification.Enum.NotificationType;
import com.esprit.campconnect.Notification.Repository.UserNotificationRepository;
import com.esprit.campconnect.Reservation.Entity.Reservation;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserNotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    public void notifyBookingConfirmed(Reservation reservation) {
        createNotification(
                reservation,
                NotificationType.BOOKING_CONFIRMED,
                "Booking confirmed",
                reservation.getEvent().getTitre() + " is confirmed for "
                        + reservation.getNombreParticipants()
                        + " guest" + (reservation.getNombreParticipants() != null && reservation.getNombreParticipants() > 1 ? "s." : "."),
                "Open booking"
        );
    }

    public void notifyWaitlistJoined(Reservation reservation) {
        createNotification(
                reservation,
                NotificationType.WAITLIST_JOINED,
                "Waitlist spot secured",
                "Your reservation for " + reservation.getEvent().getTitre()
                        + " is on the waitlist. CampConnect will alert you if a seat opens.",
                "Track waitlist"
        );
    }

    public void notifyWaitlistPromoted(Reservation reservation) {
        createNotification(
                reservation,
                NotificationType.WAITLIST_PROMOTED,
                "A seat just opened",
                "Your waitlist booking for " + reservation.getEvent().getTitre()
                        + " has been promoted to " + reservation.getStatut().name().toLowerCase() + ".",
                "View booking"
        );
    }

    public void notifyRefundProcessed(Reservation reservation) {
        BigDecimal refundAmount = reservation.getRefundAmount() != null
                ? reservation.getRefundAmount()
                : BigDecimal.ZERO;

        createNotification(
                reservation,
                NotificationType.REFUND_PROCESSED,
                "Refund processed",
                "CampConnect processed a refund of $" + refundAmount.stripTrailingZeros().toPlainString()
                        + " for " + reservation.getEvent().getTitre() + ".",
                "Review refund"
        );
    }

    @Transactional(readOnly = true)
    public List<UserNotificationResponseDTO> getNotificationsForUser(String requesterEmail) {
        Utilisateur utilisateur = getUserByEmailOrThrow(requesterEmail);
        return userNotificationRepository.findByUtilisateurIdOrderByCreatedAtDesc(utilisateur.getId()).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCountForUser(String requesterEmail) {
        Utilisateur utilisateur = getUserByEmailOrThrow(requesterEmail);
        return userNotificationRepository.countByUtilisateurIdAndReadFalse(utilisateur.getId());
    }

    public UserNotificationResponseDTO markAsRead(Long notificationId, String requesterEmail) {
        Utilisateur utilisateur = getUserByEmailOrThrow(requesterEmail);
        UserNotification notification = userNotificationRepository.findByIdAndUtilisateurId(notificationId, utilisateur.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!Boolean.TRUE.equals(notification.getRead())) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        return mapToResponseDTO(userNotificationRepository.save(notification));
    }

    public void markAllAsRead(String requesterEmail) {
        Utilisateur utilisateur = getUserByEmailOrThrow(requesterEmail);
        List<UserNotification> notifications = userNotificationRepository.findByUtilisateurIdOrderByCreatedAtDesc(utilisateur.getId());
        LocalDateTime now = LocalDateTime.now();

        notifications.stream()
                .filter(notification -> !Boolean.TRUE.equals(notification.getRead()))
                .forEach(notification -> {
                    notification.setRead(true);
                    notification.setReadAt(now);
                });

        userNotificationRepository.saveAll(notifications);
    }

    private void createNotification(
            Reservation reservation,
            NotificationType type,
            String title,
            String message,
            String actionLabel
    ) {
        if (reservation == null || reservation.getUtilisateur() == null || reservation.getEvent() == null) {
            return;
        }

        UserNotification notification = new UserNotification();
        notification.setUtilisateur(reservation.getUtilisateur());
        notification.setReservation(reservation);
        notification.setEvent(reservation.getEvent());
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setActionLabel(actionLabel);
        notification.setActionUrl("/public/events/my-reservations?focusReservation=" + reservation.getId());
        userNotificationRepository.save(notification);
    }

    private UserNotificationResponseDTO mapToResponseDTO(UserNotification notification) {
        return new UserNotificationResponseDTO(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRead(),
                notification.getCreatedAt(),
                notification.getReadAt(),
                notification.getReservation() != null ? notification.getReservation().getId() : null,
                notification.getEvent() != null ? notification.getEvent().getId() : null,
                notification.getActionLabel(),
                notification.getActionUrl()
        );
    }

    private Utilisateur getUserByEmailOrThrow(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user email is missing");
        }

        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found"));
    }
}
