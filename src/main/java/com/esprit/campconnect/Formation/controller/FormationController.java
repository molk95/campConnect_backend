package com.esprit.campconnect.Formation.controller;

import com.esprit.campconnect.Formation.dto.FormationRequestDto;
import com.esprit.campconnect.Formation.dto.FormationResponseDto;
import com.esprit.campconnect.Formation.dto.stats.FormationDetailsStatsDto;
import com.esprit.campconnect.Formation.dto.stats.FormationGlobalStatsDto;
import com.esprit.campconnect.Formation.entity.Formation;
import com.esprit.campconnect.Formation.entity.FormationLevel;
import com.esprit.campconnect.Formation.entity.FormationStatus;
import com.esprit.campconnect.Formation.service.FormationService;
import com.esprit.campconnect.Formation.service.stats.FormationStatsService;
import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/formations")
@CrossOrigin(origins = "http://localhost:4200")
public class FormationController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("dateCreation", "titre", "status", "auteurNom");

    private final FormationService formationService;
    private final FormationStatsService formationStatsService;

    public FormationController(FormationService formationService, FormationStatsService formationStatsService) {
        this.formationService = formationService;
        this.formationStatsService = formationStatsService;
    }

    @GetMapping
    public Page<FormationResponseDto> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateCreation") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) Long guideId,
            @RequestParam(required = false) FormationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Authentication authentication
    ) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le parametre page doit etre >= 0");
        }
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le parametre size doit etre entre 1 et 100");
        }
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Champ de tri non autorise");
        }

        LocalDateTime dateFromValue = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime dateToValue = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;
        if (dateFromValue != null && dateToValue != null && dateFromValue.isAfter(dateToValue)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dateFrom doit etre inferieure ou egale a dateTo");
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDir.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sortDir doit etre 'asc' ou 'desc'");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Formation> formationsPage = formationService.search(query, guideId, status, dateFromValue, dateToValue, pageable);
        Long currentUserId = getCurrentUserId(authentication);
        List<Formation> formations = formationsPage.getContent();
        Map<Long, Long> likeCounts = getLikeCounts(formations);
        Set<Long> likedByCurrentUser = getLikedFormationIds(formations, currentUserId);

        return formationsPage.map(formation -> toResponseDto(
                formation,
                likeCounts.getOrDefault(formation.getId(), 0L),
                likedByCurrentUser.contains(formation.getId())
        ));
    }

    @GetMapping("/stats")
    public FormationGlobalStatsDto getGlobalStats(Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertAdminRole(user);
        return formationStatsService.getGlobalStats();
    }

    @GetMapping("/{id}")
    public FormationResponseDto getById(@PathVariable Long id, Authentication authentication) {
        Formation formation = formationService.getById(id);
        Long currentUserId = getCurrentUserId(authentication);
        return toResponseDto(
                formation,
                formationService.getLikeCount(id),
                formationService.isLikedByUser(id, currentUserId)
        );
    }

    @GetMapping("/guide/{guideId}")
    public List<FormationResponseDto> getByGuide(@PathVariable Long guideId, Authentication authentication) {
        List<Formation> formations = formationService.getByGuide(guideId);
        Long currentUserId = getCurrentUserId(authentication);
        Map<Long, Long> likeCounts = getLikeCounts(formations);
        Set<Long> likedByCurrentUser = getLikedFormationIds(formations, currentUserId);

        return formations.stream()
                .map(formation -> toResponseDto(
                        formation,
                        likeCounts.getOrDefault(formation.getId(), 0L),
                        likedByCurrentUser.contains(formation.getId())
                ))
                .toList();
    }

    @GetMapping("/{id}/stats")
    public FormationDetailsStatsDto getByIdStats(@PathVariable Long id, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertAdminRole(user);
        return formationStatsService.getFormationStats(id);
    }

    @PostMapping
    public FormationResponseDto create(@Valid @RequestBody FormationRequestDto request, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertGuideOrAdminRole(user);

        Formation formation = new Formation();
        formation.setTitre(request.getTitre().trim());
        formation.setDescription(request.getDescription().trim());
        formation.setLevel(request.getLevel() != null ? request.getLevel() : FormationLevel.BEGINNER);
        formation.setDuration(request.getDuration() != null ? request.getDuration() : 60);
        formation.setStatus(request.getStatus() != null ? request.getStatus() : FormationStatus.DRAFT);
        formation.setAuteurEmail(user.getEmail());
        formation.setAuteurNom(user.getNom());
        formation.setGuide(user);

        Formation created = formationService.create(formation);
        return toResponseDto(created, formationService.getLikeCount(created.getId()), false);
    }

    @PutMapping("/{id}")
    public FormationResponseDto update(@PathVariable Long id,
                                       @Valid @RequestBody FormationRequestDto request,
                                       Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertGuideOrAdminRole(user);

        Formation existing = formationService.getById(id);
        assertOwnerOrAdmin(existing.getAuteurEmail(), user);

        Formation toUpdate = new Formation();
        toUpdate.setTitre(request.getTitre().trim());
        toUpdate.setDescription(request.getDescription().trim());
        toUpdate.setLevel(request.getLevel());
        toUpdate.setDuration(request.getDuration());
        toUpdate.setStatus(request.getStatus());

        Formation updated = formationService.update(id, toUpdate);
        return toResponseDto(
                updated,
                formationService.getLikeCount(updated.getId()),
                formationService.isLikedByUser(updated.getId(), user.getId())
        );
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertGuideOrAdminRole(user);

        Formation existing = formationService.getById(id);
        assertOwnerOrAdmin(existing.getAuteurEmail(), user);

        formationService.delete(id);
    }

    @PostMapping("/{id}/like")
    public FormationResponseDto like(@PathVariable Long id, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        formationService.like(id, user.getId());

        Formation formation = formationService.getById(id);
        return toResponseDto(formation, formationService.getLikeCount(id), true);
    }

    private Utilisateur requireAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Utilisateur user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        return user;
    }

    private void assertGuideOrAdminRole(Utilisateur user) {
        if (user.getRole() == Role.GUIDE || user.getRole() == Role.ADMINISTRATEUR) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action reservee aux guides et admins");
    }

    private void assertAdminRole(Utilisateur user) {
        if (user.getRole() == Role.ADMINISTRATEUR) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action reservee aux admins");
    }

    private void assertOwnerOrAdmin(String ownerEmail, Utilisateur user) {
        if (user.getRole() == Role.ADMINISTRATEUR) {
            return;
        }

        if (ownerEmail == null || !ownerEmail.equalsIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action autorisee uniquement pour l'auteur ou un admin");
        }
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Utilisateur user)) {
            return null;
        }
        return user.getId();
    }

    private Map<Long, Long> getLikeCounts(List<Formation> formations) {
        if (formations == null || formations.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> formationIds = formations.stream().map(Formation::getId).toList();
        return formationService.getLikeCountByFormationIds(formationIds);
    }

    private Set<Long> getLikedFormationIds(List<Formation> formations, Long currentUserId) {
        if (currentUserId == null || formations == null || formations.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> formationIds = formations.stream().map(Formation::getId).toList();
        return new HashSet<>(formationService.getLikedFormationIdsByUser(formationIds, currentUserId));
    }

    private FormationResponseDto toResponseDto(Formation formation, long likeCount, boolean likedByCurrentUser) {
        FormationResponseDto dto = new FormationResponseDto();
        dto.setId(formation.getId());
        dto.setTitre(formation.getTitre());
        dto.setDescription(formation.getDescription());
        dto.setLevel(formation.getLevel() != null ? formation.getLevel() : FormationLevel.BEGINNER);
        dto.setStatus(formation.getStatus() != null ? formation.getStatus() : FormationStatus.DRAFT);
        dto.setDuration(formation.getDuration() != null ? formation.getDuration() : 60);
        dto.setCreatedAt(formation.getDateCreation());
        dto.setGuideId(formation.getGuideId());
        dto.setAuteurEmail(formation.getAuteurEmail());
        dto.setAuteurNom(formation.getAuteurNom());
        dto.setLikeCount(likeCount);
        dto.setLikedByCurrentUser(likedByCurrentUser);
        return dto;
    }
}
