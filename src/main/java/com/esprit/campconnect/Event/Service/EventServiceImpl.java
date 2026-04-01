package com.esprit.campconnect.Event.Service;

import com.esprit.campconnect.Event.DTO.EventImageDTO;
import com.esprit.campconnect.Event.DTO.EventRequestDTO;
import com.esprit.campconnect.Event.DTO.EventResponseDTO;
import com.esprit.campconnect.Event.Entity.Event;
import com.esprit.campconnect.Event.Entity.EventImage;
import com.esprit.campconnect.Event.Enum.EventCategory;
import com.esprit.campconnect.Event.Enum.EventStatus;
import com.esprit.campconnect.Event.Repository.EventImageRepository;
import com.esprit.campconnect.Event.Repository.EventRepository;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import com.esprit.campconnect.config.GoogleMapsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements IEventService {

    private final EventRepository eventRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EventImageRepository eventImageRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final GoogleMapsService googleMapsService;

    @Override
    public EventResponseDTO createEvent(EventRequestDTO eventDTO, Long organizerId) {
        log.info("Creating new event organized by user: {}", organizerId);

        Utilisateur organizer = utilisateurRepository.findById(organizerId)
                .orElseThrow(() -> notFound("Organizer not found with id: " + organizerId));

        Event event = new Event();
        event.setTitre(eventDTO.getTitre());
        event.setDescription(eventDTO.getDescription());
        event.setCategorie(eventDTO.getCategorie());
        event.setStatut(EventStatus.SCHEDULED);
        event.setDateDebut(eventDTO.getDateDebut());
        event.setDateFin(eventDTO.getDateFin());
        event.setLieu(eventDTO.getLieu());
        event.setLatitude(eventDTO.getLatitude());
        event.setLongitude(eventDTO.getLongitude());
        event.setGooglePlaceId(blankToNull(eventDTO.getGooglePlaceId()));
        event.setCapaciteMax(eventDTO.getCapaciteMax());
        event.setCapaciteWaitlist(resolveWaitlistCapacity(eventDTO.getCapaciteWaitlist(), 10));
        event.setReservationApprovalRequired(resolveReservationApprovalRequired(eventDTO.getReservationApprovalRequired(), true));
        event.setPrix(eventDTO.getPrix());
        event.setDureeMinutes(eventDTO.getDureeMinutes());
        event.setBannerImage(cleanImageInput(eventDTO.getBannerImage()));
        event.setThumbnailImage(cleanImageInput(eventDTO.getThumbnailImage()));
        event.setGalleryImages(cleanImageInput(eventDTO.getGalleryImages()));
        event.setOrganizer(organizer);

        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully with id: {}", savedEvent.getId());
        return mapToResponseDTO(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long id) {
        log.info("Fetching event with id: {}", id);
        return mapToResponseDTO(getEventOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAllEvents() {
        log.info("Fetching all events");
        return eventRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getEventsByOrganizer(Long organizerId) {
        log.info("Fetching events for organizer: {}", organizerId);
        return eventRepository.findByOrganizerId(organizerId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseDTO updateEvent(Long id, EventRequestDTO eventDTO) {
        log.info("Updating event with id: {}", id);

        Event event = getEventOrThrow(id);
        if (event.getStatut() == EventStatus.ONGOING || event.getStatut() == EventStatus.COMPLETED) {
            throw badRequest("Cannot update an event that is ongoing or completed");
        }

        event.setTitre(eventDTO.getTitre());
        event.setDescription(eventDTO.getDescription());
        event.setCategorie(eventDTO.getCategorie());
        event.setDateDebut(eventDTO.getDateDebut());
        event.setDateFin(eventDTO.getDateFin());
        event.setLieu(eventDTO.getLieu());
        event.setLatitude(eventDTO.getLatitude());
        event.setLongitude(eventDTO.getLongitude());
        event.setGooglePlaceId(blankToNull(eventDTO.getGooglePlaceId()));
        event.setCapaciteMax(eventDTO.getCapaciteMax());
        event.setCapaciteWaitlist(resolveWaitlistCapacity(eventDTO.getCapaciteWaitlist(), event.getCapaciteWaitlist()));
        event.setReservationApprovalRequired(resolveReservationApprovalRequired(
                eventDTO.getReservationApprovalRequired(),
                resolveReservationApprovalRequired(event.getReservationApprovalRequired(), true)
        ));
        event.setPrix(eventDTO.getPrix());
        event.setDureeMinutes(eventDTO.getDureeMinutes());

        if (eventDTO.getBannerImage() != null) {
            event.setBannerImage(cleanImageInput(eventDTO.getBannerImage()));
        }
        if (eventDTO.getThumbnailImage() != null) {
            event.setThumbnailImage(cleanImageInput(eventDTO.getThumbnailImage()));
        }
        if (eventDTO.getGalleryImages() != null) {
            event.setGalleryImages(cleanImageInput(eventDTO.getGalleryImages()));
        }

        event.setDateModification(LocalDateTime.now());
        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated successfully");
        return mapToResponseDTO(updatedEvent);
    }

    @Override
    public void deleteEvent(Long id) {
        log.info("Deleting event with id: {}", id);

        Event event = getEventOrThrow(id);
        if (event.getStatut() == EventStatus.ONGOING || event.getStatut() == EventStatus.COMPLETED) {
            throw badRequest("Cannot delete an event that is ongoing or completed");
        }

        eventRepository.delete(event);
        log.info("Event deleted successfully");
    }

    @Override
    public EventResponseDTO uploadEventImages(Long eventId, String bannerImage, String thumbnailImage, String galleryImages) {
        log.info("Updating legacy image references for event {}", eventId);

        Event event = getEventOrThrow(eventId);
        if (bannerImage != null) {
            event.setBannerImage(cleanImageInput(bannerImage));
        }
        if (thumbnailImage != null) {
            event.setThumbnailImage(cleanImageInput(thumbnailImage));
        }
        if (galleryImages != null) {
            event.setGalleryImages(cleanImageInput(galleryImages));
        }

        event.setDateModification(LocalDateTime.now());
        return mapToResponseDTO(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getEventsByCategory(EventCategory categorie) {
        log.info("Fetching events by category: {}", categorie);
        return eventRepository.findByCategorie(categorie).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getEventsByStatus(EventStatus statut) {
        log.info("Fetching events by status: {}", statut);
        return eventRepository.findByStatut(statut).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getUpcomingEvents() {
        log.info("Fetching upcoming events");
        return eventRepository.findByDateDebutAfterOrderByDateDebut(LocalDateTime.now()).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> searchEvents(String keyword) {
        log.info("Searching events with keyword: {}", keyword);
        return eventRepository.searchByKeyword(keyword).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getEventsByLocation(String lieu) {
        log.info("Fetching events by location: {}", lieu);
        return eventRepository.findByLieuContainingIgnoreCase(lieu).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAvailableEvents() {
        log.info("Fetching available events");
        return eventRepository.findEventsWithAvailableSpots().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEventAvailable(Long eventId) {
        Event event = getEventOrThrow(eventId);
        return !event.isFullyBooked() && event.getStatut() == EventStatus.SCHEDULED;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getAvailableSeats(Long eventId) {
        return getEventOrThrow(eventId).getAvailableSeats();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getParticipantCount(Long eventId) {
        return getEventOrThrow(eventId).getParticipantsCount();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getWaitlistCount(Long eventId) {
        return getEventOrThrow(eventId).getWaitlistCount();
    }

    @Override
    public void startEvent(Long eventId) {
        log.info("Starting event with id: {}", eventId);

        Event event = getEventOrThrow(eventId);
        if (event.getStatut() != EventStatus.SCHEDULED) {
            throw badRequest("Only scheduled events can be started");
        }

        event.setStatut(EventStatus.ONGOING);
        event.setDateModification(LocalDateTime.now());
        eventRepository.save(event);
        log.info("Event started successfully");
    }

    @Override
    public void completeEvent(Long eventId) {
        log.info("Completing event with id: {}", eventId);

        Event event = getEventOrThrow(eventId);
        if (event.getStatut() != EventStatus.ONGOING) {
            throw badRequest("Only ongoing events can be completed");
        }

        event.setStatut(EventStatus.COMPLETED);
        event.setDateModification(LocalDateTime.now());
        eventRepository.save(event);
        log.info("Event completed successfully");
    }

    @Override
    public void cancelEvent(Long eventId, String reason) {
        log.info("Cancelling event with id: {}", eventId);

        Event event = getEventOrThrow(eventId);
        if (event.getStatut() == EventStatus.COMPLETED) {
            throw badRequest("Cannot cancel a completed event");
        }

        event.setStatut(EventStatus.CANCELLED);
        event.setDateModification(LocalDateTime.now());
        eventRepository.save(event);
        log.info("Event cancelled with reason: {}", reason);
    }

    @Override
    public void postponeEvent(Long eventId, LocalDateTime newStartDate, LocalDateTime newEndDate) {
        log.info("Postponing event with id: {} to {}", eventId, newStartDate);

        Event event = getEventOrThrow(eventId);
        if (event.getStatut() == EventStatus.ONGOING || event.getStatut() == EventStatus.COMPLETED) {
            throw badRequest("Cannot postpone an event that is ongoing or completed");
        }

        event.setDateDebut(newStartDate);
        event.setDateFin(newEndDate);
        event.setStatut(EventStatus.POSTPONED);
        event.setDateModification(LocalDateTime.now());
        eventRepository.save(event);
        log.info("Event postponed successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getEventsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching events between {} and {}", startDate, endDate);
        return eventRepository.findEventsBetweenDates(startDate, endDate).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateEventRevenue(Long eventId) {
        log.info("Calculating revenue for event: {}", eventId);
        return 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalParticipantsForOrganizer(Long organizerId) {
        log.info("Getting total participants for organizer: {}", organizerId);
        return eventRepository.findByOrganizerId(organizerId).stream()
                .mapToInt(event -> (int) event.getParticipantsCount())
                .sum();
    }

    @Override
    public EventResponseDTO addImageToEvent(Long eventId, MultipartFile imageFile) {
        return addImagesToEvent(eventId, List.of(imageFile), false);
    }

    @Override
    public EventResponseDTO addImagesToEvent(Long eventId, List<MultipartFile> imageFiles, boolean makePrimary) {
        List<MultipartFile> filesToStore = normalizeFiles(imageFiles);
        log.info("Adding {} image(s) to event {}", filesToStore.size(), eventId);

        if (filesToStore.isEmpty()) {
            throw badRequest("At least one image file is required");
        }

        Event event = getEventOrThrow(eventId);
        persistImages(event, filesToStore, makePrimary);
        syncEventImageReferences(event);
        return getEventById(eventId);
    }

    @Override
    public EventResponseDTO createEventWithImages(EventRequestDTO eventDTO, List<MultipartFile> imageFiles, Long organizerId) {
        List<MultipartFile> filesToStore = normalizeFiles(imageFiles);
        log.info("Creating event with {} image(s)", filesToStore.size());

        EventResponseDTO createdEvent = createEvent(eventDTO, organizerId);
        if (filesToStore.isEmpty()) {
            return createdEvent;
        }

        Event event = getEventOrThrow(createdEvent.getId());
        persistImages(event, filesToStore, false);
        syncEventImageReferences(event);
        return getEventById(createdEvent.getId());
    }

    @Override
    public void deleteEventImage(Long eventId, Long imageId) {
        log.info("Deleting image {} from event {}", imageId, eventId);

        Event event = getEventOrThrow(eventId);
        EventImage eventImage = eventImageRepository.findByIdAndEventId(imageId, eventId)
                .orElseThrow(() -> notFound("Image not found for this event"));

        String storedPath = eventImage.getImageUrl();
        eventImageRepository.delete(eventImage);
        eventImageRepository.flush();

        if (hasText(storedPath)) {
            fileStorageService.deleteFile(storedPath);
        }

        normalizeDisplayOrder(eventId);
        syncEventImageReferences(event);
        log.info("Image {} deleted successfully", imageId);
    }

    @Override
    public EventResponseDTO setImageAsPrimary(Long eventId, Long imageId) {
        log.info("Setting image {} as primary for event {}", imageId, eventId);

        Event event = getEventOrThrow(eventId);
        List<EventImage> images = getOrderedEventImages(eventId);
        if (images.isEmpty()) {
            throw notFound("No images found for this event");
        }

        boolean targetFound = false;
        for (EventImage image : images) {
            boolean shouldBePrimary = Objects.equals(image.getId(), imageId);
            if (shouldBePrimary) {
                targetFound = true;
            }
            if (!Objects.equals(image.getIsPrimary(), shouldBePrimary)) {
                image.setIsPrimary(shouldBePrimary);
                image.setLastModified(LocalDateTime.now());
            }
        }

        if (!targetFound) {
            throw notFound("Image not found for this event");
        }

        eventImageRepository.saveAll(images);
        syncEventImageReferences(event);
        log.info("Image {} set as primary for event {}", imageId, eventId);
        return getEventById(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public EventImageDTO getEventImageById(Long imageId) {
        log.info("Fetching event image with id: {}", imageId);

        EventImage eventImage = eventImageRepository.findById(imageId)
                .orElseThrow(() -> notFound("Image not found with id: " + imageId));

        return mapToImageDTO(eventImage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventImageDTO> getEventImages(Long eventId) {
        log.info("Fetching all images for event: {}", eventId);
        getEventOrThrow(eventId);
        return getOrderedEventImages(eventId).stream()
                .map(this::mapToImageDTO)
                .collect(Collectors.toList());
    }

    private EventResponseDTO mapToResponseDTO(Event event) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setTitre(event.getTitre());
        dto.setDescription(event.getDescription());
        dto.setCategorie(event.getCategorie());
        dto.setStatut(event.getStatut());
        dto.setDateDebut(event.getDateDebut());
        dto.setDateFin(event.getDateFin());
        dto.setLieu(event.getLieu());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setGooglePlaceId(blankToNull(event.getGooglePlaceId()));
        dto.setCapaciteMax(event.getCapaciteMax());
        dto.setCapaciteWaitlist(event.getCapaciteWaitlist());
        dto.setPrix(event.getPrix());
        dto.setDureeMinutes(event.getDureeMinutes());
        String googleMapsUrl = googleMapsService.buildGoogleMapsUrl(event);
        dto.setGoogleMapsUrl(googleMapsUrl);
        dto.setHasMapLocation(googleMapsUrl != null);
        dto.setReservationApprovalRequired(resolveReservationApprovalRequired(event.getReservationApprovalRequired(), true));

        if (event.getOrganizer() != null) {
            dto.setOrganizerId(event.getOrganizer().getId());
            dto.setOrganizerNom(event.getOrganizer().getNom());
            dto.setOrganizerEmail(event.getOrganizer().getEmail());
        }

        dto.setDateCreation(event.getDateCreation());
        dto.setDateModification(event.getDateModification());
        dto.setParticipantsCount(event.getParticipantsCount());
        dto.setWaitlistCount(event.getWaitlistCount());
        dto.setAvailableSeats(event.getAvailableSeats());
        dto.setIsFullyBooked(event.isFullyBooked());
        dto.setIsAlmostFull(event.isAlmostFull());
        dto.setOccupancyRate(event.getOccupancyRate());

        List<EventImageDTO> imageDTOs = event.getId() == null
                ? List.of()
                : getOrderedEventImages(event.getId()).stream()
                .map(this::mapToImageDTO)
                .collect(Collectors.toList());
        dto.setImages(imageDTOs);
        dto.setImageCount(imageDTOs.size());

        List<String> uploadedImageUrls = imageDTOs.stream()
                .map(EventImageDTO::getImageUrl)
                .filter(this::hasText)
                .collect(Collectors.toList());
        List<String> legacyGalleryUrls = normalizeGalleryReferences(event.getGalleryImages());

        String uploadedPrimaryImageUrl = imageDTOs.stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()) && hasText(image.getImageUrl()))
                .map(EventImageDTO::getImageUrl)
                .findFirst()
                .orElse(uploadedImageUrls.stream().findFirst().orElse(null));

        String normalizedBanner = normalizeImageReference(event.getBannerImage());
        String normalizedThumbnail = normalizeImageReference(event.getThumbnailImage());
        String fallbackBanner = buildFallbackImageDataUri(event, false);
        String fallbackThumbnail = buildFallbackImageDataUri(event, true);

        dto.setPrimaryImageUrl(firstNonBlank(
                uploadedPrimaryImageUrl,
                normalizedBanner,
                normalizedThumbnail,
                legacyGalleryUrls.stream().findFirst().orElse(null),
                fallbackBanner
        ));
        dto.setBannerImage(firstNonBlank(
                uploadedPrimaryImageUrl,
                normalizedBanner,
                normalizedThumbnail,
                fallbackBanner
        ));
        dto.setThumbnailImage(firstNonBlank(
                uploadedPrimaryImageUrl,
                normalizedThumbnail,
                normalizedBanner,
                fallbackThumbnail
        ));

        List<String> galleryImageUrls = !uploadedImageUrls.isEmpty()
                ? uploadedImageUrls
                : !legacyGalleryUrls.isEmpty()
                ? legacyGalleryUrls
                : List.of(dto.getBannerImage());

        dto.setGalleryImageUrls(galleryImageUrls);
        dto.setGalleryImages(serializeGalleryUrls(galleryImageUrls));

        return dto;
    }

    private EventImageDTO mapToImageDTO(EventImage eventImage) {
        EventImageDTO dto = new EventImageDTO();
        dto.setId(eventImage.getId());
        dto.setImageName(eventImage.getImageName());
        dto.setImageUrl(buildEventImageContentUrl(eventImage.getId()));
        dto.setDescription(eventImage.getDescription());
        dto.setIsPrimary(eventImage.getIsPrimary());
        dto.setDisplayOrder(eventImage.getDisplayOrder());
        dto.setMimeType(eventImage.getMimeType());
        dto.setFileSize(eventImage.getFileSize());
        if (eventImage.getEvent() != null) {
            dto.setEventId(eventImage.getEvent().getId());
        }
        dto.setUploadDate(eventImage.getUploadDate());
        dto.setLastModified(eventImage.getLastModified());
        dto.setIsAvailable(true);
        return dto;
    }

    private Boolean resolveReservationApprovalRequired(Boolean requestedValue, boolean fallbackValue) {
        return requestedValue != null ? requestedValue : fallbackValue;
    }

    private void persistImages(Event event, List<MultipartFile> imageFiles, boolean makePrimary) {
        List<String> storedPaths = new ArrayList<>();
        List<EventImage> existingImages = getOrderedEventImages(event.getId());
        boolean hasPrimary = existingImages.stream().anyMatch(image -> Boolean.TRUE.equals(image.getIsPrimary()));

        if (makePrimary && !existingImages.isEmpty()) {
            existingImages.forEach(image -> image.setIsPrimary(false));
            eventImageRepository.saveAll(existingImages);
            hasPrimary = false;
        }

        int nextDisplayOrder = getNextDisplayOrder(event.getId());
        boolean assignPrimaryToNextSavedImage = !hasPrimary;

        try {
            for (MultipartFile imageFile : imageFiles) {
                byte[] imageBytes = imageFile.getBytes();
                String storedPath = fileStorageService.storeFile(imageFile, imageBytes);
                storedPaths.add(storedPath);

                EventImage eventImage = new EventImage();
                eventImage.setEvent(event);
                eventImage.setImageName(resolveImageName(imageFile));
                eventImage.setImageUrl(storedPath);
                eventImage.setImageData(Base64.getEncoder().encodeToString(imageBytes));
                eventImage.setMimeType(resolveMimeType(imageFile));
                eventImage.setFileSize(imageFile.getSize());
                eventImage.setDisplayOrder(nextDisplayOrder++);
                eventImage.setIsPrimary(assignPrimaryToNextSavedImage);
                eventImage.setUploadDate(LocalDateTime.now());
                eventImage.setLastModified(LocalDateTime.now());

                eventImageRepository.save(eventImage);
                assignPrimaryToNextSavedImage = false;
            }
        } catch (IllegalArgumentException exception) {
            storedPaths.forEach(fileStorageService::deleteFile);
            throw badRequest(exception.getMessage());
        } catch (IOException exception) {
            storedPaths.forEach(fileStorageService::deleteFile);
            throw serverError("Failed to upload event image", exception);
        }
    }

    private void syncEventImageReferences(Event event) {
        List<EventImage> orderedImages = getOrderedEventImages(event.getId());
        if (orderedImages.isEmpty()) {
            event.setBannerImage(null);
            event.setThumbnailImage(null);
            event.setGalleryImages(null);
            event.setDateModification(LocalDateTime.now());
            eventRepository.save(event);
            return;
        }

        EventImage primaryImage = orderedImages.stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                .findFirst()
                .orElse(orderedImages.get(0));

        boolean primaryAdjusted = false;
        for (EventImage image : orderedImages) {
            boolean shouldBePrimary = Objects.equals(image.getId(), primaryImage.getId());
            if (!Objects.equals(image.getIsPrimary(), shouldBePrimary)) {
                image.setIsPrimary(shouldBePrimary);
                image.setLastModified(LocalDateTime.now());
                primaryAdjusted = true;
            }
        }

        if (primaryAdjusted) {
            eventImageRepository.saveAll(orderedImages);
        }

        String primaryImageUrl = buildEventImageContentUrl(primaryImage.getId());
        List<String> galleryUrls = orderedImages.stream()
                .map(image -> buildEventImageContentUrl(image.getId()))
                .collect(Collectors.toList());

        event.setBannerImage(primaryImageUrl);
        event.setThumbnailImage(primaryImageUrl);
        event.setGalleryImages(serializeGalleryUrls(galleryUrls));
        event.setDateModification(LocalDateTime.now());
        eventRepository.save(event);
    }

    private void normalizeDisplayOrder(Long eventId) {
        List<EventImage> orderedImages = getOrderedEventImages(eventId);
        boolean updated = false;

        for (int index = 0; index < orderedImages.size(); index++) {
            EventImage image = orderedImages.get(index);
            if (!Objects.equals(image.getDisplayOrder(), index)) {
                image.setDisplayOrder(index);
                image.setLastModified(LocalDateTime.now());
                updated = true;
            }
        }

        if (updated) {
            eventImageRepository.saveAll(orderedImages);
        }
    }

    private List<EventImage> getOrderedEventImages(Long eventId) {
        return eventImageRepository.findByEventId(eventId).stream()
                .sorted(Comparator
                        .comparing((EventImage image) -> Boolean.TRUE.equals(image.getIsPrimary()))
                        .reversed()
                        .thenComparing(EventImage::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(EventImage::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private List<MultipartFile> normalizeFiles(List<MultipartFile> imageFiles) {
        if (imageFiles == null) {
            return List.of();
        }

        return imageFiles.stream()
                .filter(Objects::nonNull)
                .filter(file -> !file.isEmpty())
                .collect(Collectors.toList());
    }

    private int getNextDisplayOrder(Long eventId) {
        Integer maxDisplayOrder = eventImageRepository.getMaxDisplayOrderForEvent(eventId);
        return (maxDisplayOrder == null ? -1 : maxDisplayOrder) + 1;
    }

    private int resolveWaitlistCapacity(Integer requestedCapacity, Integer fallbackCapacity) {
        if (requestedCapacity != null) {
            return requestedCapacity;
        }
        if (fallbackCapacity != null) {
            return fallbackCapacity;
        }
        return 10;
    }

    private List<String> normalizeGalleryReferences(String galleryImages) {
        String normalizedGallery = blankToNull(galleryImages);
        if (normalizedGallery == null) {
            return List.of();
        }

        try {
            if (normalizedGallery.trim().startsWith("[")) {
                List<String> values = objectMapper.readValue(normalizedGallery, new TypeReference<List<String>>() { });
                return values.stream()
                        .map(this::normalizeImageReference)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
            }
        } catch (JsonProcessingException exception) {
            log.debug("Failed to parse galleryImages as JSON for event response", exception);
        }

        String normalizedSingleValue = normalizeImageReference(normalizedGallery);
        if (normalizedSingleValue == null) {
            return List.of();
        }

        return List.of(normalizedSingleValue);
    }

    private String serializeGalleryUrls(List<String> galleryUrls) {
        try {
            return objectMapper.writeValueAsString(galleryUrls.stream()
                    .filter(this::hasText)
                    .distinct()
                    .collect(Collectors.toList()));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize event gallery URLs", exception);
        }
    }

    private String resolveImageName(MultipartFile imageFile) {
        return firstNonBlank(imageFile.getOriginalFilename(), "event-image");
    }

    private String resolveMimeType(MultipartFile imageFile) {
        return firstNonBlank(imageFile.getContentType(), "application/octet-stream");
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> notFound("Event not found with id: " + eventId));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String buildFallbackImageDataUri(Event event, boolean thumbnail) {
        String[] palette = getCategoryPalette(event.getCategorie());
        int width = thumbnail ? 400 : 1200;
        int height = thumbnail ? 250 : 400;
        int titleFontSize = thumbnail ? 24 : 46;
        int categoryFontSize = thumbnail ? 14 : 24;
        String safeTitle = escapeSvgText(firstNonBlank(event.getTitre(), "CampConnect Event"));
        String safeCategory = escapeSvgText(event.getCategorie() != null ? event.getCategorie().name().replace('_', ' ') : "EVENT");
        String safeLocation = escapeSvgText(firstNonBlank(event.getLieu(), "Discover the next adventure"));

        String svg = String.format(
                "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 %d %d'>"
                        + "<defs>"
                        + "<linearGradient id='bg' x1='0' y1='0' x2='1' y2='1'>"
                        + "<stop offset='0%%' stop-color='%s'/>"
                        + "<stop offset='100%%' stop-color='%s'/>"
                        + "</linearGradient>"
                        + "</defs>"
                        + "<rect width='100%%' height='100%%' fill='url(#bg)'/>"
                        + "<circle cx='%d' cy='%d' r='%d' fill='%s' fill-opacity='0.22'/>"
                        + "<circle cx='%d' cy='%d' r='%d' fill='%s' fill-opacity='0.18'/>"
                        + "<text x='7%%' y='20%%' font-family='Arial, sans-serif' font-size='%d' font-weight='700' fill='white' letter-spacing='2'>%s</text>"
                        + "<text x='7%%' y='56%%' font-family='Arial, sans-serif' font-size='%d' font-weight='700' fill='white'>%s</text>"
                        + "<text x='7%%' y='74%%' font-family='Arial, sans-serif' font-size='%d' fill='white' fill-opacity='0.9'>%s</text>"
                        + "</svg>",
                width,
                height,
                palette[0],
                palette[1],
                (int) (width * 0.82),
                (int) (height * 0.25),
                thumbnail ? 90 : 130,
                palette[2],
                (int) (width * 0.16),
                (int) (height * 0.82),
                thumbnail ? 70 : 110,
                palette[3],
                categoryFontSize,
                safeCategory,
                titleFontSize,
                safeTitle,
                thumbnail ? 15 : 22,
                safeLocation
        );

        return "data:image/svg+xml;charset=UTF-8," + URLEncoder.encode(svg, StandardCharsets.UTF_8);
    }

    private String[] getCategoryPalette(EventCategory category) {
        if (category == null) {
            return new String[]{"#1f4f46", "#0c2a24", "#d4af37", "#ffffff"};
        }

        return switch (category) {
            case ADVENTURE -> new String[]{"#2e6f40", "#112b1b", "#f6c453", "#dcefdc"};
            case CAMPING_ACTIVITY -> new String[]{"#6b4f2a", "#2f2210", "#f2d7a1", "#efe2c4"};
            case GUIDED_TOUR -> new String[]{"#1c5d99", "#0a2540", "#91c8f6", "#e6f4ff"};
            case RESTORATION -> new String[]{"#9c3d1e", "#43180c", "#ffcb77", "#ffe7c2"};
            case SOCIAL_EVENT -> new String[]{"#8b1e3f", "#3b0c1a", "#ff9fb2", "#ffe0e8"};
            case WELLNESS -> new String[]{"#3d7a6a", "#173730", "#c4f1de", "#ecfff7"};
            case WORKSHOP -> new String[]{"#5a4ea1", "#241d49", "#d4cbff", "#f0eeff"};
            case EDUCATIONAL -> new String[]{"#7c5a12", "#382805", "#f4dd92", "#fff5d7"};
        };
    }

    private String escapeSvgText(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String cleanImageInput(String value) {
        return blankToNull(value);
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeImageReference(String value) {
        String normalizedValue = blankToNull(value);
        if (normalizedValue == null) {
            return null;
        }

        if (normalizedValue.startsWith("data:")
                || normalizedValue.startsWith("http://")
                || normalizedValue.startsWith("https://")) {
            return normalizedValue;
        }

        String sanitized = normalizedValue.replace('\\', '/');
        if (sanitized.startsWith("/api/")) {
            return sanitized;
        }
        if (sanitized.startsWith("api/")) {
            return "/" + sanitized;
        }
        if (sanitized.startsWith("/events/")) {
            return "/api" + sanitized;
        }
        if (sanitized.startsWith("events/")) {
            return "/api/" + sanitized;
        }

        return null;
    }

    private String buildEventImageContentUrl(Long imageId) {
        return "/api/events/images/" + imageId + "/content";
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException serverError(String message, Exception cause) {
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}
