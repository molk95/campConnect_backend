package com.esprit.campconnect.Reservation.Repository;

import com.esprit.campconnect.Reservation.Entity.Reservation;
import com.esprit.campconnect.Reservation.Enum.ReservationStatus;
import com.esprit.campconnect.Reservation.Enum.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Find reservations by user
    List<Reservation> findByUtilisateurId(Long utilisateurId);

    // Find reservations by event
    List<Reservation> findByEventId(Long eventId);

    // Find reservations by status
    List<Reservation> findByStatut(ReservationStatus statut);

    // Find user's reservation for a specific event
    @Query("SELECT r FROM Reservation r WHERE r.utilisateur.id = :utilisateurId AND r.event.id = :eventId")
    Optional<Reservation> findByUtilisateurAndEvent(
            @Param("utilisateurId") Long utilisateurId,
            @Param("eventId") Long eventId
    );

    // Find all reservations for an event with specific status
    @Query("SELECT r FROM Reservation r WHERE r.event.id = :eventId AND r.statut = :statut")
    List<Reservation> findByEventAndStatus(
            @Param("eventId") Long eventId,
            @Param("statut") ReservationStatus statut
    );

    // Find pending reservations (for admin approval)
    List<Reservation> findByStatutAndEstEnAttenteTrue(ReservationStatus statut);

    // Find unpaid reservations
    List<Reservation> findByStatutPaiementNot(PaymentStatus statutPaiement);

    // Find waitlist reservations for specific event
    @Query("SELECT r FROM Reservation r WHERE r.event.id = :eventId AND r.estEnAttente = true AND r.statut = 'PENDING' ORDER BY r.dateCreation ASC")
    List<Reservation> findWaitlistByEvent(@Param("eventId") Long eventId);

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.event e
            WHERE r.estEnAttente = true
              AND r.statut = 'PENDING'
              AND e.dateDebut <= :cutoff
            ORDER BY e.dateDebut ASC, r.dateCreation ASC
            """)
    List<Reservation> findExpiredWaitlistReservations(@Param("cutoff") LocalDateTime cutoff);

    // Find reservations created after specific date
    List<Reservation> findByDateCreationAfter(LocalDateTime dateCreation);

    Optional<Reservation> findByTransactionId(String transactionId);

    Optional<Reservation> findByStripeInvoiceId(String stripeInvoiceId);

    // Count confirmed/paid reservations for event
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.event.id = :eventId AND r.statut IN ('CONFIRMED', 'PAID')")
    Long countConfirmedReservations(@Param("eventId") Long eventId);

    // Find user's cancelled reservations
    @Query("SELECT r FROM Reservation r WHERE r.utilisateur.id = :utilisateurId AND r.statut = 'CANCELLED'")
    List<Reservation> findUserCancelledReservations(@Param("utilisateurId") Long utilisateurId);

    // Count total revenue for event
    @Query("SELECT COALESCE(SUM(r.prixTotal), 0) FROM Reservation r WHERE r.event.id = :eventId AND r.statutPaiement = 'PAID'")
    Double calculateEventRevenue(@Param("eventId") Long eventId);

    // Find refundable reservations
    @Query("SELECT r FROM Reservation r WHERE r.statut IN ('CANCELLED', 'NO_SHOW') AND r.statutPaiement = 'PAID'")
    List<Reservation> findRefundableReservations();
}
