package com.esprit.campconnect.Reservation.Entity;

import lombok.*;
import com.esprit.campconnect.Event.Entity.Event;
import com.esprit.campconnect.Reservation.Enum.PaymentStatus;
import com.esprit.campconnect.Reservation.Enum.ReservationStatus;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"utilisateur", "event"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_offer_id")
    private PromotionOffer promotionOffer;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus statut;

    @Column(nullable = false)
    private Integer nombreParticipants; // Number of participants in this reservation (1 or more)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePriceTotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private String promoCode;

    @Column(length = 255)
    private String discountLabel;

    @Column(nullable = false)
    private Boolean discountAutoApplied = false;

    @Column(nullable = false)
    private BigDecimal prixTotal; // Total price for all participants

    @Column(nullable = false)
    private Boolean estEnAttente = false; // Is this reservation on waitlist?

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus statutPaiement = PaymentStatus.UNPAID;

    @Column(columnDefinition = "TEXT")
    private String remarques;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dateModification = LocalDateTime.now();

    // Payment tracking
    private LocalDateTime datePaiement;

    private String transactionId; // External payment gateway transaction ID

    private String stripeInvoiceId;

    private String stripeInvoiceNumber;

    @Column(columnDefinition = "TEXT")
    private String invoiceHostedUrl;

    @Column(columnDefinition = "TEXT")
    private String invoicePdfUrl;

    @Column(precision = 12, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    private Integer refundPercentage = 0;

    private LocalDateTime cancelledAt;

    private LocalDateTime refundedAt;

    @Column(columnDefinition = "TEXT")
    private String cancellationReason;

    // Pre-update hook to update the modification date
    @PreUpdate
    public void preUpdate() {
        dateModification = LocalDateTime.now();
    }

    // Helper method to calculate refund amount
    public BigDecimal calculateRefund() {
        if (statutPaiement == PaymentStatus.REFUNDED) {
            return BigDecimal.ZERO;
        }
        return prixTotal.subtract(refundAmount != null ? refundAmount : BigDecimal.ZERO).max(BigDecimal.ZERO);
    }
}
