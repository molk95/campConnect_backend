package com.esprit.campconnect.Reservation.Service;

import com.esprit.campconnect.Event.Entity.Event;
import com.esprit.campconnect.Event.Repository.EventRepository;
import com.esprit.campconnect.Reservation.DTO.PromotionOfferRequestDTO;
import com.esprit.campconnect.Reservation.DTO.PromotionOfferResponseDTO;
import com.esprit.campconnect.Reservation.Entity.PromotionOffer;
import com.esprit.campconnect.Reservation.Enum.PromotionDiscountType;
import com.esprit.campconnect.Reservation.Repository.PromotionOfferRepository;
import com.esprit.campconnect.Reservation.Repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionOfferServiceTest {

    @Mock
    private PromotionOfferRepository promotionOfferRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EventRepository eventRepository;

    private PromotionOfferService promotionOfferService;

    @BeforeEach
    void setUp() {
        promotionOfferService = new PromotionOfferService(
                promotionOfferRepository,
                reservationRepository,
                eventRepository
        );
    }

    @Test
    void evaluateReservationPricingAppliesManualPromoCode() {
        Event event = buildEvent();
        PromotionOffer promoCode = buildPromotion(
                "Camp Season Saver",
                "CAMP15",
                PromotionDiscountType.PERCENTAGE,
                new BigDecimal("15"),
                false,
                null
        );

        when(promotionOfferRepository.findByCodeIgnoreCase("CAMP15")).thenReturn(Optional.of(promoCode));
        PromotionOfferService.PromotionEvaluationResult result =
                promotionOfferService.evaluateReservationPricing(event, 2, "camp15", true);

        assertThat(result.getBasePriceTotal()).isEqualByComparingTo("120.00");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("18.00");
        assertThat(result.getTotalPrice()).isEqualByComparingTo("102.00");
        assertThat(result.getAppliedPromoCode()).isEqualTo("CAMP15");
        assertThat(result.getDiscountLabel()).contains("15% off");
        assertThat(result.isAutoApplied()).isFalse();
    }

    @Test
    void evaluateReservationPricingFallsBackToBestAutoPromotion() {
        Event event = buildEvent();
        PromotionOffer groupOffer = buildPromotion(
                "Group Explorer Deal",
                "GROUP4",
                PromotionDiscountType.PERCENTAGE,
                new BigDecimal("12"),
                true,
                4
        );

        when(promotionOfferRepository.findByAutoApplyTrueAndActiveTrueOrderByDateCreationDesc())
                .thenReturn(List.of(groupOffer));
        PromotionOfferService.PromotionEvaluationResult result =
                promotionOfferService.evaluateReservationPricing(event, 4, null, false);

        assertThat(result.getBasePriceTotal()).isEqualByComparingTo("240.00");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("28.80");
        assertThat(result.getTotalPrice()).isEqualByComparingTo("211.20");
        assertThat(result.getAppliedPromotion()).isNotNull();
        assertThat(result.isAutoApplied()).isTrue();
        assertThat(result.getValidationMessage()).contains("applied automatically");
    }

    @Test
    void evaluateReservationPricingRejectsPromoCodeForAnotherEvent() {
        Event event = buildEvent(19L, "Sunrise camp");
        Event otherEvent = buildEvent(88L, "Desert trek");
        PromotionOffer scopedPromo = buildPromotion(
                "Desert Weekend",
                "DESERT20",
                PromotionDiscountType.PERCENTAGE,
                new BigDecimal("20"),
                false,
                null
        );
        scopedPromo.setAppliesToAllEvents(false);
        scopedPromo.setEligibleEvents(new LinkedHashSet<>(List.of(otherEvent)));

        when(promotionOfferRepository.findByCodeIgnoreCase("DESERT20")).thenReturn(Optional.of(scopedPromo));
        when(promotionOfferRepository.countTargetedEvent(17L, 19L)).thenReturn(0L);

        assertThatThrownBy(() -> promotionOfferService.evaluateReservationPricing(event, 2, "DESERT20", true))
                .hasMessageContaining("This promo code is not valid for this event.");
    }

    @Test
    void createPromotionSupportsEventScopedPromoCodes() {
        Event sunriseCamp = buildEvent(19L, "Sunrise camp");
        Event desertTrek = buildEvent(24L, "Desert trek");

        PromotionOfferRequestDTO requestDTO = new PromotionOfferRequestDTO();
        requestDTO.setName("Weekend Bundle");
        requestDTO.setCode("WEEKEND25");
        requestDTO.setDescription("Save on selected weekend events");
        requestDTO.setDiscountType(PromotionDiscountType.PERCENTAGE);
        requestDTO.setDiscountValue(new BigDecimal("25"));
        requestDTO.setAppliesToAllEvents(false);
        requestDTO.setEventIds(List.of(19L, 24L));
        requestDTO.setAutoApply(false);
        requestDTO.setDiscoverable(true);
        requestDTO.setActive(true);

        when(promotionOfferRepository.findByCodeIgnoreCase("WEEKEND25")).thenReturn(Optional.empty());
        when(eventRepository.findAllById(any())).thenReturn(List.of(sunriseCamp, desertTrek));
        when(promotionOfferRepository.save(any(PromotionOffer.class))).thenAnswer(invocation -> {
            PromotionOffer promotionOffer = invocation.getArgument(0);
            promotionOffer.setId(52L);
            return promotionOffer;
        });
        when(promotionOfferRepository.findTargetedEventSummariesByPromotionIds(eq(List.of(52L))))
                .thenReturn(List.of(
                        targetedEventSummary(52L, sunriseCamp),
                        targetedEventSummary(52L, desertTrek)
                ));
        when(reservationRepository.countByPromotionOfferId(52L)).thenReturn(0L);

        PromotionOfferResponseDTO responseDTO = promotionOfferService.createPromotion(requestDTO);

        assertThat(responseDTO.getAppliesToAllEvents()).isFalse();
        assertThat(responseDTO.getEligibleEventIds()).containsExactly(19L, 24L);
        assertThat(responseDTO.getEligibleEvents())
                .extracting(event -> event.getTitre())
                .containsExactly("Sunrise camp", "Desert trek");
    }

    @Test
    void deletePromotionRemovesReservationReferencesAndTargetLinksBeforeDeletingPromotionRow() {
        when(promotionOfferRepository.existsById(9L)).thenReturn(true);
        when(promotionOfferRepository.deletePromotionByIdNative(9L)).thenReturn(1);

        promotionOfferService.deletePromotion(9L);

        verify(reservationRepository).clearPromotionOfferReferences(9L);
        verify(promotionOfferRepository).deleteTargetedEventsByPromotionId(9L);
        verify(promotionOfferRepository).deletePromotionByIdNative(9L);
    }

    @Test
    void deletePromotionAlsoDeletesPromotionsThatWereAlreadyUsed() {
        when(promotionOfferRepository.existsById(3L)).thenReturn(true);
        when(promotionOfferRepository.deletePromotionByIdNative(3L)).thenReturn(1);

        promotionOfferService.deletePromotion(3L);

        verify(reservationRepository).clearPromotionOfferReferences(3L);
        verify(promotionOfferRepository).deleteTargetedEventsByPromotionId(3L);
        verify(promotionOfferRepository).deletePromotionByIdNative(3L);
    }

    private Event buildEvent() {
        return buildEvent(19L, "Sunrise camp");
    }

    private Event buildEvent(Long id, String title) {
        Event event = new Event();
        event.setId(id);
        event.setPrix(new BigDecimal("60"));
        event.setTitre(title);
        event.setLieu("Sahara Gate");
        event.setDateDebut(LocalDateTime.of(2026, 5, 1, 10, 0));
        event.setDateFin(LocalDateTime.of(2026, 5, 1, 15, 0));
        return event;
    }

    private PromotionOffer buildPromotion(
            String name,
            String code,
            PromotionDiscountType discountType,
            BigDecimal discountValue,
            boolean autoApply,
            Integer minimumParticipants
    ) {
        PromotionOffer promotionOffer = new PromotionOffer();
        promotionOffer.setId(17L);
        promotionOffer.setName(name);
        promotionOffer.setCode(code);
        promotionOffer.setDiscountType(discountType);
        promotionOffer.setDiscountValue(discountValue);
        promotionOffer.setAutoApply(autoApply);
        promotionOffer.setActive(true);
        promotionOffer.setDiscoverable(true);
        promotionOffer.setAppliesToAllEvents(true);
        promotionOffer.setMinimumParticipants(minimumParticipants);
        promotionOffer.setStartsAt(LocalDateTime.now().minusDays(1));
        promotionOffer.setEndsAt(LocalDateTime.now().plusDays(30));
        promotionOffer.setDateCreation(LocalDateTime.now().minusDays(10));
        return promotionOffer;
    }

    private PromotionOfferRepository.PromotionTargetEventSummaryView targetedEventSummary(Long promotionId, Event event) {
        return new PromotionOfferRepository.PromotionTargetEventSummaryView() {
            @Override
            public Long getPromotionOfferId() {
                return promotionId;
            }

            @Override
            public Long getEventId() {
                return event.getId();
            }

            @Override
            public String getTitre() {
                return event.getTitre();
            }

            @Override
            public String getLieu() {
                return event.getLieu();
            }

            @Override
            public LocalDateTime getDateDebut() {
                return event.getDateDebut();
            }

            @Override
            public LocalDateTime getDateFin() {
                return event.getDateFin();
            }
        };
    }
}
