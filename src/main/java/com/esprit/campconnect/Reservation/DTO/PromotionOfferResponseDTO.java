package com.esprit.campconnect.Reservation.DTO;

import com.esprit.campconnect.Reservation.Enum.PromotionDiscountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PromotionOfferResponseDTO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private PromotionDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumSubtotal;
    private Integer minimumParticipants;
    private Boolean autoApply;
    private Boolean discoverable;
    private Boolean active;
    private Boolean appliesToAllEvents;
    private List<Long> eligibleEventIds;
    private List<PromotionEventSummaryDTO> eligibleEvents;
    private Boolean currentlyAvailable;
    private Integer maxRedemptions;
    private Long usageCount;
    private Long remainingRedemptions;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    @JsonProperty("eventIds")
    public List<Long> getEventIds() {
        return eligibleEventIds;
    }

    @JsonProperty("targetedEvents")
    public List<PromotionEventSummaryDTO> getTargetedEvents() {
        return eligibleEvents;
    }
}
