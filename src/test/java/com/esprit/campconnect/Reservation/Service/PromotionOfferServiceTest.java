package com.esprit.campconnect.Reservation.Service;

import com.esprit.campconnect.Event.Entity.Event;
import com.esprit.campconnect.Event.Repository.EventRepository;
import com.esprit.campconnect.Reservation.Entity.PromotionOffer;
import com.esprit.campconnect.Reservation.Enum.PromotionDiscountType;
import com.esprit.campconnect.Reservation.Repository.PromotionOfferRepository;
import com.esprit.campconnect.Reservation.Repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionOfferServiceTest {

    @Mock
    private PromotionOfferRepository promotionOfferRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private EventRepository eventRepository;

    private PromotionOfferService service;

    @BeforeEach
    void setUp() {
        service = new PromotionOfferService(promotionOfferRepository, reservationRepository, eventRepository);
    }

    @Test
    void evaluateReservationPricingAppliesManualPercentagePromoCode() {
        Event event = pricedEvent("100.00");
        PromotionOffer offer = promotion(1L, "Spring Deal", "spring10", PromotionDiscountType.PERCENTAGE, "10.00");
        when(promotionOfferRepository.findByCodeIgnoreCase("SPRING10")).thenReturn(Optional.of(offer));

        PromotionOfferService.PromotionEvaluationResult result =
                service.evaluateReservationPricing(event, 3, " spring10 ", true);

        assertThat(result.getBasePriceTotal()).isEqualByComparingTo("300.00");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("30.00");
        assertThat(result.getTotalPrice()).isEqualByComparingTo("270.00");
        assertThat(result.getAppliedPromoCode()).isEqualTo("SPRING10");
        assertThat(result.isAutoApplied()).isFalse();
    }

    @Test
    void evaluateReservationPricingChoosesBestAutoPromotion() {
        Event event = pricedEvent("100.00");
        PromotionOffer percentageOffer = promotion(1L, "Ten percent", null, PromotionDiscountType.PERCENTAGE, "10.00");
        percentageOffer.setAutoApply(true);
        PromotionOffer fixedOffer = promotion(2L, "Twenty five off", null, PromotionDiscountType.FIXED_AMOUNT, "25.00");
        fixedOffer.setAutoApply(true);
        when(promotionOfferRepository.findByAutoApplyTrueAndActiveTrueOrderByDateCreationDesc())
                .thenReturn(List.of(percentageOffer, fixedOffer));

        PromotionOfferService.PromotionEvaluationResult result =
                service.evaluateReservationPricing(event, 2, null, false);

        assertThat(result.getBasePriceTotal()).isEqualByComparingTo("200.00");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("25.00");
        assertThat(result.getTotalPrice()).isEqualByComparingTo("175.00");
        assertThat(result.getAppliedPromotion()).isSameAs(fixedOffer);
        assertThat(result.isAutoApplied()).isTrue();
    }

    @Test
    void evaluateReservationPricingRejectsUnknownStrictPromoCode() {
        Event event = pricedEvent("80.00");
        when(promotionOfferRepository.findByCodeIgnoreCase("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.evaluateReservationPricing(event, 1, "missing", true))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Promo code not found");
    }

    @Test
    void evaluateReservationPricingIgnoresPromotionWithMissingDiscountType() {
        Event event = pricedEvent("75.00");
        PromotionOffer offer = promotion(1L, "Broken promo", "broken", null, "50.00");
        when(promotionOfferRepository.findByCodeIgnoreCase("BROKEN")).thenReturn(Optional.of(offer));

        PromotionOfferService.PromotionEvaluationResult result =
                service.evaluateReservationPricing(event, 2, "broken", true);

        assertThat(result.getBasePriceTotal()).isEqualByComparingTo("150.00");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("0.00");
        assertThat(result.getTotalPrice()).isEqualByComparingTo("150.00");
    }

    @Test
    void evaluateReservationPricingRejectsInactiveStrictPromoCode() {
        Event event = pricedEvent("80.00");
        PromotionOffer offer = promotion(1L, "Paused promo", "paused", PromotionDiscountType.PERCENTAGE, "20.00");
        offer.setActive(false);
        when(promotionOfferRepository.findByCodeIgnoreCase("PAUSED")).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> service.evaluateReservationPricing(event, 1, "paused", true))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void evaluateReservationPricingRejectsPromoForDifferentEvent() {
        Event event = pricedEvent("80.00");
        PromotionOffer offer = promotion(1L, "Private promo", "private", PromotionDiscountType.PERCENTAGE, "20.00");
        offer.setAppliesToAllEvents(false);
        when(promotionOfferRepository.findByCodeIgnoreCase("PRIVATE")).thenReturn(Optional.of(offer));
        when(promotionOfferRepository.countTargetedEvent(1L, 10L)).thenReturn(0L);

        assertThatThrownBy(() -> service.evaluateReservationPricing(event, 1, "private", true))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not valid for this event");
    }

    @Test
    void evaluateReservationPricingRejectsPromotionBeforeStartDate() {
        Event event = pricedEvent("80.00");
        PromotionOffer offer = promotion(1L, "Future promo", "future", PromotionDiscountType.PERCENTAGE, "20.00");
        offer.setStartsAt(LocalDateTime.now().plusDays(1));
        when(promotionOfferRepository.findByCodeIgnoreCase("FUTURE")).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> service.evaluateReservationPricing(event, 1, "future", true))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("has not started");
    }

    @Test
    void evaluateReservationPricingRejectsMinimumRequirementsAndRedemptionLimit() {
        Event event = pricedEvent("40.00");
        PromotionOffer participantsOffer = promotion(1L, "Group promo", "group", PromotionDiscountType.PERCENTAGE, "20.00");
        participantsOffer.setMinimumParticipants(3);
        when(promotionOfferRepository.findByCodeIgnoreCase("GROUP")).thenReturn(Optional.of(participantsOffer));

        assertThatThrownBy(() -> service.evaluateReservationPricing(event, 2, "group", true))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("at least 3 guests");

        PromotionOffer subtotalOffer = promotion(2L, "Big basket", "basket", PromotionDiscountType.FIXED_AMOUNT, "15.00");
        subtotalOffer.setMinimumSubtotal(new BigDecimal("100.00"));
        when(promotionOfferRepository.findByCodeIgnoreCase("BASKET")).thenReturn(Optional.of(subtotalOffer));

        assertThatThrownBy(() -> service.evaluateReservationPricing(event, 2, "basket", true))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("subtotal of at least $100");

        PromotionOffer limitedOffer = promotion(3L, "Limited promo", "limit", PromotionDiscountType.FIXED_AMOUNT, "15.00");
        limitedOffer.setMaxRedemptions(2);
        when(promotionOfferRepository.findByCodeIgnoreCase("LIMIT")).thenReturn(Optional.of(limitedOffer));
        when(reservationRepository.countByPromotionOfferId(3L)).thenReturn(2L);

        assertThatThrownBy(() -> service.evaluateReservationPricing(event, 2, "limit", true))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("campaign limit");
    }

    @Test
    void evaluateReservationPricingIgnoresNullAutoPromotionCandidate() {
        Event event = pricedEvent("100.00");
        List<PromotionOffer> autoPromotions = new ArrayList<>();
        autoPromotions.add(null);
        when(promotionOfferRepository.findByAutoApplyTrueAndActiveTrueOrderByDateCreationDesc())
                .thenReturn(autoPromotions);

        PromotionOfferService.PromotionEvaluationResult result =
                service.evaluateReservationPricing(event, 1, null, false);

        assertThat(result.getDiscountAmount()).isEqualByComparingTo("0.00");
        assertThat(result.getTotalPrice()).isEqualByComparingTo("100.00");
        assertThat(result.getValidationMessage()).isNull();
    }

    private Event pricedEvent(String unitPrice) {
        Event event = new Event();
        event.setId(10L);
        event.setPrix(new BigDecimal(unitPrice));
        return event;
    }

    private PromotionOffer promotion(
            Long id,
            String name,
            String code,
            PromotionDiscountType discountType,
            String discountValue
    ) {
        PromotionOffer promotion = new PromotionOffer();
        promotion.setId(id);
        promotion.setName(name);
        promotion.setCode(code);
        promotion.setDiscountType(discountType);
        promotion.setDiscountValue(new BigDecimal(discountValue));
        promotion.setActive(true);
        promotion.setAppliesToAllEvents(true);
        promotion.setAutoApply(false);
        return promotion;
    }
}
