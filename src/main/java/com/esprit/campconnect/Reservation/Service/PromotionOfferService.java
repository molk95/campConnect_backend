package com.esprit.campconnect.Reservation.Service;

import com.esprit.campconnect.Event.Entity.Event;
import com.esprit.campconnect.Event.Enum.EventStatus;
import com.esprit.campconnect.Event.Repository.EventRepository;
import com.esprit.campconnect.Reservation.DTO.PromotionEventSummaryDTO;
import com.esprit.campconnect.Reservation.DTO.PromotionOfferRequestDTO;
import com.esprit.campconnect.Reservation.DTO.PromotionOfferResponseDTO;
import com.esprit.campconnect.Reservation.DTO.PromotionPreviewDTO;
import com.esprit.campconnect.Reservation.Entity.PromotionOffer;
import com.esprit.campconnect.Reservation.Enum.PromotionDiscountType;
import com.esprit.campconnect.Reservation.Repository.PromotionOfferRepository;
import com.esprit.campconnect.Reservation.Repository.ReservationRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionOfferService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final PromotionOfferRepository promotionOfferRepository;
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public PromotionPreviewDTO previewReservationPricing(Long eventId, Integer numberOfParticipants, String promoCode) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        validateEventAcceptsReservations(event);

        PromotionEvaluationResult evaluationResult =
                evaluateReservationPricing(event, numberOfParticipants, promoCode, false);

        return mapToPreviewDTO(event, numberOfParticipants, evaluationResult);
    }

    @Transactional(readOnly = true)
    public List<PromotionOfferResponseDTO> getPublicActivePromotions() {
        return getPublicActivePromotions(null);
    }

    @Transactional(readOnly = true)
    public List<PromotionOfferResponseDTO> getPublicActivePromotions(Long eventId) {
        Event event = eventId != null ? getEventOrThrow(eventId) : null;
        List<PromotionOffer> promotions = promotionOfferRepository.findByDiscoverableTrueAndActiveTrueOrderByAutoApplyDescDateCreationDesc();
        PromotionTargetContext targetContext = loadPromotionTargetContext(promotions);
        java.util.Map<Long, Long> usageCountsByPromotionId = loadUsageCounts(promotions);

        return promotions.stream()
                .filter(offer -> isCurrentlyAvailable(
                        offer,
                        event,
                        usageCountsByPromotionId.getOrDefault(offer.getId(), 0L),
                        targetContext
                ))
                .sorted(Comparator
                        .comparing((PromotionOffer offer) -> Boolean.TRUE.equals(offer.getAutoApply()))
                        .reversed()
                        .thenComparing(PromotionOffer::getDateCreation, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(offer -> mapToResponseDTO(
                        offer,
                        usageCountsByPromotionId.getOrDefault(offer.getId(), 0L),
                        targetContext
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PromotionOfferResponseDTO> getAllPromotions() {
        return getAllPromotions(null);
    }

    @Transactional(readOnly = true)
    public List<PromotionOfferResponseDTO> getAllPromotions(Long eventId) {
        Event event = eventId != null ? getEventOrThrow(eventId) : null;
        List<PromotionOffer> promotions = promotionOfferRepository.findAll();
        PromotionTargetContext targetContext = loadPromotionTargetContext(promotions);
        java.util.Map<Long, Long> usageCountsByPromotionId = loadUsageCounts(promotions);

        return promotions.stream()
                .filter(offer -> event == null || appliesToEvent(offer, event, targetContext))
                .sorted(Comparator.comparing(PromotionOffer::getDateCreation, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(offer -> mapToResponseDTO(
                        offer,
                        usageCountsByPromotionId.getOrDefault(offer.getId(), 0L),
                        targetContext
                ))
                .toList();
    }

    private void validateEventAcceptsReservations(Event event) {
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        LocalDateTime now = LocalDateTime.now();
        if (event.getStatut() == EventStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled events cannot accept reservations");
        }

        if (event.getDateFin() != null && !event.getDateFin().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This event has already finished");
        }

        if (event.getDateDebut() != null && !event.getDateDebut().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservations close once the event has started");
        }
    }

    @Transactional(readOnly = true)
    public PromotionOfferResponseDTO getPromotionById(Long promotionId) {
        PromotionOffer promotionOffer = promotionOfferRepository.findById(promotionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found"));

        return mapToResponseDTO(
                promotionOffer,
                getUsageCount(promotionOffer),
                loadPromotionTargetContext(List.of(promotionOffer))
        );
    }

    public PromotionOfferResponseDTO createPromotion(PromotionOfferRequestDTO requestDTO) {
        PromotionTargetSelection targetSelection = validatePromotionRequest(requestDTO, null);

        PromotionOffer promotionOffer = new PromotionOffer();
        populatePromotionOffer(promotionOffer, requestDTO, targetSelection);
        PromotionOffer savedPromotion = promotionOfferRepository.save(promotionOffer);
        syncPromotionTargets(savedPromotion.getId(), targetSelection.eventIds());
        return mapToResponseDTO(
                savedPromotion,
                getUsageCount(savedPromotion),
                loadPromotionTargetContext(List.of(savedPromotion))
        );
    }

    public PromotionOfferResponseDTO updatePromotion(Long promotionId, PromotionOfferRequestDTO requestDTO) {
        PromotionOffer promotionOffer = promotionOfferRepository.findById(promotionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found"));

        PromotionTargetSelection targetSelection = validatePromotionRequest(requestDTO, promotionId);
        populatePromotionOffer(promotionOffer, requestDTO, targetSelection);
        PromotionOffer savedPromotion = promotionOfferRepository.save(promotionOffer);
        syncPromotionTargets(savedPromotion.getId(), targetSelection.eventIds());
        return mapToResponseDTO(
                savedPromotion,
                getUsageCount(savedPromotion),
                loadPromotionTargetContext(List.of(savedPromotion))
        );
    }

    public void deletePromotion(Long promotionId) {
        if (!promotionOfferRepository.existsById(promotionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found");
        }

        reservationRepository.clearPromotionOfferReferences(promotionId);

        try {
            promotionOfferRepository.deleteTargetedEventsByPromotionId(promotionId);
        } catch (DataAccessException ignored) {
            // Keep delete resilient for local databases that do not yet have the join table.
        }
        int deletedRows = promotionOfferRepository.deletePromotionByIdNative(promotionId);
        if (deletedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found");
        }
    }

    @Transactional(readOnly = true)
    public PromotionEvaluationResult evaluateReservationPricing(
            Event event,
            Integer numberOfParticipants,
            String promoCode,
            boolean strictPromoCode
    ) {
        if (event == null || event.getPrix() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event pricing is not available");
        }

        if (numberOfParticipants == null || numberOfParticipants <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one participant is required");
        }

        BigDecimal unitPrice = safeMoney(event.getPrix());
        BigDecimal basePriceTotal = unitPrice.multiply(BigDecimal.valueOf(numberOfParticipants)).setScale(2, RoundingMode.HALF_UP);
        LocalDateTime now = LocalDateTime.now();

        String normalizedPromoCode = normalizeCode(promoCode);
        PromotionOffer appliedPromotion = null;
        boolean invalidPromoCode = false;
        String validationMessage = null;

        if (StringUtils.hasText(normalizedPromoCode)) {
            PromotionOffer manualPromotion = promotionOfferRepository.findByCodeIgnoreCase(normalizedPromoCode)
                    .orElse(null);

            if (manualPromotion == null) {
                if (strictPromoCode) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promo code not found");
                }
                invalidPromoCode = true;
                validationMessage = "That promo code could not be found. Active group offers still apply automatically.";
            } else {
                String eligibilityIssue = getEligibilityIssue(
                        manualPromotion,
                        event,
                        basePriceTotal,
                        numberOfParticipants,
                        now,
                        null
                );
                if (eligibilityIssue != null) {
                    if (strictPromoCode) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, eligibilityIssue);
                    }
                    invalidPromoCode = true;
                    validationMessage = eligibilityIssue + " Eligible group offers still apply automatically when available.";
                } else {
                    appliedPromotion = manualPromotion;
                }
            }
        }

        if (appliedPromotion == null) {
            appliedPromotion = findBestAutoPromotion(event, basePriceTotal, numberOfParticipants, now);
            if (appliedPromotion != null && !invalidPromoCode) {
                validationMessage = buildAutoApplyMessage(appliedPromotion);
            }
        }

        BigDecimal discountAmount = appliedPromotion != null
                ? calculateDiscountAmount(basePriceTotal, appliedPromotion)
                : BigDecimal.ZERO;
        BigDecimal totalPrice = basePriceTotal.subtract(discountAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        return new PromotionEvaluationResult(
                unitPrice,
                basePriceTotal,
                discountAmount,
                totalPrice,
                appliedPromotion,
                appliedPromotion != null ? normalizeCode(appliedPromotion.getCode()) : null,
                appliedPromotion != null ? buildDiscountLabel(appliedPromotion) : null,
                appliedPromotion != null && Boolean.TRUE.equals(appliedPromotion.getAutoApply()),
                invalidPromoCode,
                validationMessage
        );
    }

    private PromotionOffer findBestAutoPromotion(
            Event event,
            BigDecimal basePriceTotal,
            Integer numberOfParticipants,
            LocalDateTime now
    ) {
        return promotionOfferRepository.findByAutoApplyTrueAndActiveTrueOrderByDateCreationDesc().stream()
                .filter(offer -> getEligibilityIssue(offer, event, basePriceTotal, numberOfParticipants, now, null) == null)
                .max(Comparator
                        .comparing((PromotionOffer offer) -> calculateDiscountAmount(basePriceTotal, offer))
                        .thenComparing(PromotionOffer::getDateCreation, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private String getEligibilityIssue(
            PromotionOffer promotionOffer,
            Event event,
            BigDecimal basePriceTotal,
            Integer numberOfParticipants,
            LocalDateTime now,
            Long usageCountOverride
    ) {
        return getEligibilityIssue(
                promotionOffer,
                event,
                basePriceTotal,
                numberOfParticipants,
                now,
                usageCountOverride,
                null
        );
    }

    private String getEligibilityIssue(
            PromotionOffer promotionOffer,
            Event event,
            BigDecimal basePriceTotal,
            Integer numberOfParticipants,
            LocalDateTime now,
            Long usageCountOverride,
            PromotionTargetContext targetContext
    ) {
        if (promotionOffer == null) {
            return "Promotion not found";
        }

        if (!Boolean.TRUE.equals(promotionOffer.getActive())) {
            return "This promotion is not active right now.";
        }

        if (!appliesToEvent(promotionOffer, event, targetContext)) {
            return "This promo code is not valid for this event.";
        }

        if (promotionOffer.getStartsAt() != null && now.isBefore(promotionOffer.getStartsAt())) {
            return "This promotion has not started yet.";
        }

        if (promotionOffer.getEndsAt() != null && now.isAfter(promotionOffer.getEndsAt())) {
            return "This promotion has already ended.";
        }

        if (promotionOffer.getMinimumParticipants() != null
                && numberOfParticipants < promotionOffer.getMinimumParticipants()) {
            return "This promotion requires at least "
                    + promotionOffer.getMinimumParticipants()
                    + " guests.";
        }

        if (promotionOffer.getMinimumSubtotal() != null
                && basePriceTotal.compareTo(safeMoney(promotionOffer.getMinimumSubtotal())) < 0) {
            return "This promotion requires a subtotal of at least "
                    + formatMoney(promotionOffer.getMinimumSubtotal())
                    + ".";
        }

        if (hasReachedMaxRedemptions(promotionOffer, usageCountOverride)) {
            return "This promotion has already reached its campaign limit.";
        }

        return null;
    }

    private boolean isCurrentlyAvailable(PromotionOffer promotionOffer) {
        return isCurrentlyAvailable(promotionOffer, null);
    }

    private boolean isCurrentlyAvailable(PromotionOffer promotionOffer, Event event) {
        return isCurrentlyAvailable(promotionOffer, event, null);
    }

    private boolean isCurrentlyAvailable(PromotionOffer promotionOffer, Event event, Long usageCountOverride) {
        return isCurrentlyAvailable(promotionOffer, event, usageCountOverride, null);
    }

    private boolean isCurrentlyAvailable(
            PromotionOffer promotionOffer,
            Event event,
            Long usageCountOverride,
            PromotionTargetContext targetContext
    ) {
        return getEligibilityIssue(
                promotionOffer,
                event,
                safeMoney(promotionOffer.getMinimumSubtotal()),
                Math.max(1, promotionOffer.getMinimumParticipants() != null ? promotionOffer.getMinimumParticipants() : 1),
                LocalDateTime.now(),
                usageCountOverride,
                targetContext
        ) == null;
    }

    private boolean appliesToEvent(PromotionOffer promotionOffer, Event event) {
        return appliesToEvent(promotionOffer, event, null);
    }

    private boolean appliesToEvent(
            PromotionOffer promotionOffer,
            Event event,
            PromotionTargetContext targetContext
    ) {
        if (promotionOffer == null || Boolean.TRUE.equals(promotionOffer.getAppliesToAllEvents()) || event == null) {
            return true;
        }

        Long promotionId = promotionOffer.getId();
        Long eventId = event.getId();
        if (promotionId == null || eventId == null) {
            return false;
        }

        if (targetContext != null) {
            List<Long> targetedEventIds = targetContext.eventIdsByPromotionId().getOrDefault(promotionId, List.of());
            return targetedEventIds.contains(eventId);
        }

        try {
            return promotionOfferRepository.countTargetedEvent(promotionId, eventId) > 0;
        } catch (DataAccessException ignored) {
            return false;
        }
    }

    private boolean hasReachedMaxRedemptions(PromotionOffer promotionOffer) {
        return hasReachedMaxRedemptions(promotionOffer, null);
    }

    private boolean hasReachedMaxRedemptions(PromotionOffer promotionOffer, Long usageCountOverride) {
        if (promotionOffer == null || promotionOffer.getId() == null || promotionOffer.getMaxRedemptions() == null) {
            return false;
        }

        long usageCount = usageCountOverride != null ? usageCountOverride : getUsageCount(promotionOffer);
        return usageCount >= promotionOffer.getMaxRedemptions();
    }

    private long getUsageCount(PromotionOffer promotionOffer) {
        if (promotionOffer == null || promotionOffer.getId() == null) {
            return 0L;
        }

        return getUsageCount(promotionOffer.getId());
    }

    private long getUsageCount(Long promotionId) {
        if (promotionId == null) {
            return 0L;
        }

        return reservationRepository.countByPromotionOfferId(promotionId);
    }

    private BigDecimal calculateDiscountAmount(BigDecimal basePriceTotal, PromotionOffer promotionOffer) {
        if (promotionOffer == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal safeBasePrice = safeMoney(basePriceTotal);
        BigDecimal calculatedDiscount = switch (promotionOffer.getDiscountType()) {
            case PERCENTAGE -> safeBasePrice
                    .multiply(safeMoney(promotionOffer.getDiscountValue()))
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> safeMoney(promotionOffer.getDiscountValue());
        };

        return calculatedDiscount.min(safeBasePrice).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private PromotionPreviewDTO mapToPreviewDTO(
            Event event,
            Integer numberOfParticipants,
            PromotionEvaluationResult evaluationResult
    ) {
        return new PromotionPreviewDTO(
                event != null ? event.getId() : null,
                numberOfParticipants,
                evaluationResult.getUnitPrice(),
                evaluationResult.getBasePriceTotal(),
                evaluationResult.getDiscountAmount(),
                evaluationResult.getTotalPrice(),
                evaluationResult.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0,
                evaluationResult.isAutoApplied(),
                evaluationResult.isInvalidPromoCode(),
                evaluationResult.getAppliedPromoCode(),
                evaluationResult.getAppliedPromotion() != null ? evaluationResult.getAppliedPromotion().getName() : null,
                evaluationResult.getDiscountLabel(),
                evaluationResult.getValidationMessage()
        );
    }

    private PromotionOfferResponseDTO mapToResponseDTO(PromotionOffer promotionOffer) {
        return mapToResponseDTO(promotionOffer, getUsageCount(promotionOffer));
    }

    private PromotionOfferResponseDTO mapToResponseDTO(PromotionOffer promotionOffer, long usageCount) {
        return mapToResponseDTO(promotionOffer, usageCount, null);
    }

    private PromotionOfferResponseDTO mapToResponseDTO(
            PromotionOffer promotionOffer,
            long usageCount,
            PromotionTargetContext targetContext
    ) {
        Long remainingRedemptions = promotionOffer.getMaxRedemptions() == null
                ? null
                : Math.max(0L, promotionOffer.getMaxRedemptions() - usageCount);
        List<PromotionEventSummaryDTO> eligibleEvents = mapToEligibleEvents(promotionOffer, targetContext);
        List<Long> eligibleEventIds = eligibleEvents.stream()
                .map(PromotionEventSummaryDTO::getId)
                .filter(Objects::nonNull)
                .toList();

        return new PromotionOfferResponseDTO(
                promotionOffer.getId(),
                promotionOffer.getName(),
                promotionOffer.getCode(),
                promotionOffer.getDescription(),
                promotionOffer.getDiscountType(),
                promotionOffer.getDiscountValue(),
                promotionOffer.getMinimumSubtotal(),
                promotionOffer.getMinimumParticipants(),
                promotionOffer.getAutoApply(),
                promotionOffer.getDiscoverable(),
                promotionOffer.getActive(),
                promotionOffer.getAppliesToAllEvents(),
                eligibleEventIds,
                eligibleEvents,
                isCurrentlyAvailable(promotionOffer, null, usageCount, targetContext),
                promotionOffer.getMaxRedemptions(),
                usageCount,
                remainingRedemptions,
                promotionOffer.getStartsAt(),
                promotionOffer.getEndsAt(),
                promotionOffer.getDateCreation(),
                promotionOffer.getDateModification()
        );
    }

    private List<PromotionEventSummaryDTO> mapToEligibleEvents(
            PromotionOffer promotionOffer,
            PromotionTargetContext targetContext
    ) {
        if (promotionOffer == null || Boolean.TRUE.equals(promotionOffer.getAppliesToAllEvents())) {
            return List.of();
        }

        if (promotionOffer.getId() == null) {
            return List.of();
        }

        if (targetContext != null) {
            return targetContext.eventSummariesByPromotionId()
                    .getOrDefault(promotionOffer.getId(), List.of());
        }

        return loadPromotionTargetContext(List.of(promotionOffer)).eventSummariesByPromotionId()
                .getOrDefault(promotionOffer.getId(), List.of());
    }

    private void populatePromotionOffer(
            PromotionOffer promotionOffer,
            PromotionOfferRequestDTO requestDTO,
            PromotionTargetSelection targetSelection
    ) {
        promotionOffer.setName(requestDTO.getName().trim());
        promotionOffer.setCode(normalizeCode(requestDTO.getCode()));
        promotionOffer.setDescription(StringUtils.hasText(requestDTO.getDescription()) ? requestDTO.getDescription().trim() : null);
        promotionOffer.setDiscountType(requestDTO.getDiscountType());
        promotionOffer.setDiscountValue(safeMoney(requestDTO.getDiscountValue()));
        promotionOffer.setMinimumSubtotal(requestDTO.getMinimumSubtotal() != null ? safeMoney(requestDTO.getMinimumSubtotal()) : null);
        promotionOffer.setMinimumParticipants(requestDTO.getMinimumParticipants());
        promotionOffer.setAutoApply(Boolean.TRUE.equals(requestDTO.getAutoApply()));
        promotionOffer.setDiscoverable(requestDTO.getDiscoverable() == null || Boolean.TRUE.equals(requestDTO.getDiscoverable()));
        promotionOffer.setActive(requestDTO.getActive() == null || Boolean.TRUE.equals(requestDTO.getActive()));
        promotionOffer.setAppliesToAllEvents(targetSelection.appliesToAllEvents());
        promotionOffer.setStartsAt(requestDTO.getStartsAt());
        promotionOffer.setEndsAt(requestDTO.getEndsAt());
        promotionOffer.setMaxRedemptions(requestDTO.getMaxRedemptions());
    }

    private PromotionTargetSelection validatePromotionRequest(PromotionOfferRequestDTO requestDTO, Long currentPromotionId) {
        if (requestDTO.getStartsAt() != null
                && requestDTO.getEndsAt() != null
                && requestDTO.getEndsAt().isBefore(requestDTO.getStartsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promotion end date must be after the start date");
        }

        String normalizedCode = normalizeCode(requestDTO.getCode());
        if (!Boolean.TRUE.equals(requestDTO.getAutoApply()) && !StringUtils.hasText(normalizedCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A promo code is required unless the discount is auto-applied");
        }

        if (StringUtils.hasText(normalizedCode)) {
            PromotionOffer existingPromotion = promotionOfferRepository.findByCodeIgnoreCase(normalizedCode).orElse(null);
            if (existingPromotion != null && !Objects.equals(existingPromotion.getId(), currentPromotionId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This promo code already exists");
            }
        }

        if (requestDTO.getDiscountType() == PromotionDiscountType.PERCENTAGE
                && requestDTO.getDiscountValue() != null
                && requestDTO.getDiscountValue().compareTo(HUNDRED) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Percentage discounts cannot exceed 100%");
        }

        Set<Long> eventIds = normalizeEventIds(requestDTO.getEventIds());
        boolean appliesToAllEvents = requestDTO.getAppliesToAllEvents() == null
                ? eventIds.isEmpty()
                : Boolean.TRUE.equals(requestDTO.getAppliesToAllEvents());

        if (appliesToAllEvents && !eventIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Choose either all events or a specific list of events for this promotion"
            );
        }

        if (!appliesToAllEvents && eventIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one event must be selected when the promotion is not valid for all events"
            );
        }

        if (!appliesToAllEvents) {
            List<Event> resolvedEvents = eventRepository.findAllById(eventIds);
            if (resolvedEvents.size() != eventIds.size()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more selected events were not found");
            }
        }

        return new PromotionTargetSelection(appliesToAllEvents, eventIds);
    }

    private java.util.Map<Long, Long> loadUsageCounts(List<PromotionOffer> promotions) {
        if (promotions == null || promotions.isEmpty()) {
            return java.util.Map.of();
        }

        List<Long> promotionIds = promotions.stream()
                .map(PromotionOffer::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (promotionIds.isEmpty()) {
            return java.util.Map.of();
        }

        java.util.Map<Long, Long> usageCountsByPromotionId = new java.util.HashMap<>();
        for (ReservationRepository.PromotionUsageCountView usageCount : reservationRepository.countPromotionUsages(promotionIds)) {
            if (usageCount.getPromotionOfferId() == null) {
                continue;
            }
            usageCountsByPromotionId.put(
                    usageCount.getPromotionOfferId(),
                    usageCount.getUsageCount() != null ? usageCount.getUsageCount() : 0L
            );
        }
        return usageCountsByPromotionId;
    }

    private PromotionTargetContext loadPromotionTargetContext(List<PromotionOffer> promotions) {
        if (promotions == null || promotions.isEmpty()) {
            return PromotionTargetContext.empty();
        }

        List<Long> promotionIds = promotions.stream()
                .map(PromotionOffer::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (promotionIds.isEmpty()) {
            return PromotionTargetContext.empty();
        }

        Map<Long, List<PromotionEventSummaryDTO>> eventSummariesByPromotionId = new HashMap<>();
        Map<Long, List<Long>> eventIdsByPromotionId = new HashMap<>();

        for (Long promotionId : promotionIds) {
            eventSummariesByPromotionId.put(promotionId, new ArrayList<>());
            eventIdsByPromotionId.put(promotionId, new ArrayList<>());
        }

        try {
            for (PromotionOfferRepository.PromotionTargetEventSummaryView row
                    : promotionOfferRepository.findTargetedEventSummariesByPromotionIds(promotionIds)) {
                if (row.getPromotionOfferId() == null || row.getEventId() == null) {
                    continue;
                }

                eventSummariesByPromotionId.computeIfAbsent(row.getPromotionOfferId(), ignored -> new ArrayList<>())
                        .add(new PromotionEventSummaryDTO(
                                row.getEventId(),
                                row.getTitre(),
                                row.getLieu(),
                                row.getDateDebut(),
                                row.getDateFin()
                        ));
                eventIdsByPromotionId.computeIfAbsent(row.getPromotionOfferId(), ignored -> new ArrayList<>())
                        .add(row.getEventId());
            }
        } catch (DataAccessException ignored) {
            return PromotionTargetContext.empty();
        }

        Map<Long, List<PromotionEventSummaryDTO>> normalizedEventSummaries = new HashMap<>();
        eventSummariesByPromotionId.forEach((promotionId, summaries) -> normalizedEventSummaries.put(
                promotionId,
                List.copyOf(summaries)
        ));

        Map<Long, List<Long>> normalizedEventIds = new HashMap<>();
        eventIdsByPromotionId.forEach((promotionId, eventIds) -> normalizedEventIds.put(
                promotionId,
                List.copyOf(eventIds)
        ));

        return new PromotionTargetContext(
                Map.copyOf(normalizedEventIds),
                Map.copyOf(normalizedEventSummaries)
        );
    }

    private void syncPromotionTargets(Long promotionId, Set<Long> eventIds) {
        if (promotionId == null) {
            return;
        }

        try {
            promotionOfferRepository.deleteTargetedEventsByPromotionId(promotionId);
        } catch (DataAccessException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Promotion targets could not be updated. Please verify the promotion target table exists.",
                    ex
            );
        }
        if (eventIds == null || eventIds.isEmpty()) {
            return;
        }

        for (Long eventId : eventIds) {
            if (eventId == null) {
                continue;
            }
            try {
                promotionOfferRepository.insertTargetedEvent(promotionId, eventId);
            } catch (DataAccessException ex) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Promotion targets could not be updated. Please verify the promotion target table exists.",
                        ex
                );
            }
        }
    }

    private Set<Long> normalizeEventIds(List<Long> rawEventIds) {
        if (rawEventIds == null || rawEventIds.isEmpty()) {
            return Set.of();
        }

        return rawEventIds.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private String buildDiscountLabel(PromotionOffer promotionOffer) {
        if (promotionOffer == null) {
            return null;
        }

        String valueLabel = promotionOffer.getDiscountType() == PromotionDiscountType.PERCENTAGE
                ? safeMoney(promotionOffer.getDiscountValue()).stripTrailingZeros().toPlainString() + "% off"
                : formatMoney(promotionOffer.getDiscountValue()) + " off";

        return promotionOffer.getName() + " (" + valueLabel + ")";
    }

    private String buildAutoApplyMessage(PromotionOffer promotionOffer) {
        if (promotionOffer == null) {
            return null;
        }

        if (promotionOffer.getMinimumParticipants() != null && promotionOffer.getMinimumParticipants() > 1) {
            return promotionOffer.getName()
                    + " was applied automatically for groups of "
                    + promotionOffer.getMinimumParticipants()
                    + "+ guests.";
        }

        return promotionOffer.getName() + " was applied automatically to this booking.";
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String formatMoney(BigDecimal value) {
        return "$" + safeMoney(value).stripTrailingZeros().toPlainString();
    }

    private String normalizeCode(String rawCode) {
        return StringUtils.hasText(rawCode) ? rawCode.trim().toUpperCase(Locale.ROOT) : null;
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    @Getter
    @RequiredArgsConstructor
    public static class PromotionEvaluationResult {
        private final BigDecimal unitPrice;
        private final BigDecimal basePriceTotal;
        private final BigDecimal discountAmount;
        private final BigDecimal totalPrice;
        private final PromotionOffer appliedPromotion;
        private final String appliedPromoCode;
        private final String discountLabel;
        private final boolean autoApplied;
        private final boolean invalidPromoCode;
        private final String validationMessage;
    }

    private record PromotionTargetSelection(boolean appliesToAllEvents, Set<Long> eventIds) {
    }

    private record PromotionTargetContext(
            Map<Long, List<Long>> eventIdsByPromotionId,
            Map<Long, List<PromotionEventSummaryDTO>> eventSummariesByPromotionId
    ) {
        private static PromotionTargetContext empty() {
            return new PromotionTargetContext(Map.of(), Map.of());
        }
    }
}
