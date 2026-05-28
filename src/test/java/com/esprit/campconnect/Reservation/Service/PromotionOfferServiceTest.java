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
