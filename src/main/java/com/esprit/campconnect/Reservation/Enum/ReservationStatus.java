package com.esprit.campconnect.Reservation.Enum;

public enum ReservationStatus {
    PENDING,      // Reservation request submitted, awaiting confirmation
    CONFIRMED,    // Reservation confirmed
    PAID,         // Payment received
    NO_SHOW,      // User didn't show up for the event
    CANCELLED,    // Reservation cancelled by user or admin
    REFUNDED      // Refund processed
}
