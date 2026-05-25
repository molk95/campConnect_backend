package com.esprit.campconnect.Reservation.Service;

import com.esprit.campconnect.Event.Entity.Event;
import com.esprit.campconnect.Event.Repository.EventRepository;
import com.esprit.campconnect.Reservation.DTO.PaymentProcessDTO;
import com.esprit.campconnect.Reservation.DTO.ReservationResponseDTO;
import com.esprit.campconnect.Reservation.Entity.Reservation;
import com.esprit.campconnect.Reservation.Enum.PaymentStatus;
import com.esprit.campconnect.Reservation.Enum.ReservationStatus;
import com.esprit.campconnect.Reservation.Repository.ReservationRepository;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private StripeGatewayService stripeGatewayService;
    @Mock
    private ReservationReceiptPdfService reservationReceiptPdfService;
    @Mock
    private ReservationCalendarService reservationCalendarService;
    @Mock
    private PromotionOfferService promotionOfferService;
    @Mock
    private UserNotificationService userNotificationService;

    private ReservationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReservationServiceImpl(
                reservationRepository,
                eventRepository,
                utilisateurRepository,
                stripeGatewayService,
                reservationReceiptPdfService,
                reservationCalendarService,
                promotionOfferService,
                userNotificationService
        );
    }

    @Test
    void processPaymentMarksReservationPaidAndConfirmed() {
        Reservation reservation = reservation();
        when(reservationRepository.findById(99L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentProcessDTO payment = new PaymentProcessDTO();
        payment.setReservationId(99L);
        payment.setStatutPaiement(PaymentStatus.PAID);
        payment.setTransactionId("txn_123");

        ReservationResponseDTO response = service.processPayment(payment);

        assertThat(reservation.getStatutPaiement()).isEqualTo(PaymentStatus.PAID);
        assertThat(reservation.getStatut()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.getTransactionId()).isEqualTo("txn_123");
        assertThat(reservation.getDatePaiement()).isNotNull();
        assertThat(response.getStatut()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(response.getStatutPaiement()).isEqualTo(PaymentStatus.PAID);
        verify(userNotificationService).notifyPaymentConfirmed(reservation);
    }

    @Test
    void processPaymentKeepsWaitlistReservationPendingAfterPaidPayment() {
        Reservation reservation = reservation();
        reservation.setEstEnAttente(true);
        when(reservationRepository.findById(99L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentProcessDTO payment = new PaymentProcessDTO();
        payment.setReservationId(99L);
        payment.setStatutPaiement(PaymentStatus.PAID);
        payment.setTransactionId("txn_waitlist");

        service.processPayment(payment);

        assertThat(reservation.getStatut()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservation.getStatutPaiement()).isEqualTo(PaymentStatus.PAID);
        verify(userNotificationService).notifyPaymentConfirmed(reservation);
    }

    private Reservation reservation() {
        Utilisateur user = new Utilisateur();
        user.setId(7L);
        user.setNom("Iheb");
        user.setEmail("iheb@example.com");

        Event event = new Event();
        event.setId(42L);
        event.setTitre("Camp night");
        event.setLieu("Bizerte");
        event.setDateDebut(LocalDateTime.now().plusDays(3));
        event.setDateFin(LocalDateTime.now().plusDays(3).plusHours(2));

        Reservation reservation = new Reservation();
        reservation.setId(99L);
        reservation.setUtilisateur(user);
        reservation.setEvent(event);
        reservation.setStatut(ReservationStatus.PENDING);
        reservation.setStatutPaiement(PaymentStatus.UNPAID);
        reservation.setEstEnAttente(false);
        reservation.setNombreParticipants(2);
        reservation.setPrixTotal(new BigDecimal("60.00"));
        reservation.setBasePriceTotal(new BigDecimal("60.00"));
        reservation.setDiscountAmount(BigDecimal.ZERO);
        reservation.setDiscountAutoApplied(false);
        reservation.setDateCreation(LocalDateTime.now().minusDays(1));
        reservation.setDateModification(LocalDateTime.now().minusDays(1));
        return reservation;
    }
}
