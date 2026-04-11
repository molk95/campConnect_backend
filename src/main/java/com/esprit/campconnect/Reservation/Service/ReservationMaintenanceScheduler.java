package com.esprit.campconnect.Reservation.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationMaintenanceScheduler {

    private final IReservationService reservationService;

    @Scheduled(cron = "${app.reservations.waitlist-reconciliation-cron:0 * * * * *}")
    public void reconcileExpiredWaitlistReservations() {
        log.debug("Running scheduled waitlist reconciliation");
        reservationService.reconcileExpiredWaitlistReservations();
    }

    @Scheduled(cron = "${app.reservations.waitlist-offer-reconciliation-cron:0 */10 * * * *}")
    public void reconcileExpiredWaitlistOffers() {
        log.debug("Running scheduled waitlist offer reconciliation");
        reservationService.reconcileExpiredWaitlistOffers();
    }

    @Scheduled(cron = "${app.reservations.reminder-cron:0 */15 * * * *}")
    public void dispatchUpcomingReservationReminders() {
        log.debug("Running scheduled reservation reminder dispatch");
        reservationService.dispatchUpcomingReservationReminders();
    }

    @Scheduled(cron = "${app.reservations.feedback-request-cron:0 0 * * * *}")
    public void requestPostEventFeedback() {
        log.debug("Running scheduled post-event feedback requests");
        reservationService.requestPostEventFeedback();
    }
}
