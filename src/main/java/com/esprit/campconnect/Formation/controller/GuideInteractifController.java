package com.esprit.campconnect.Formation.controller;

import com.esprit.campconnect.Formation.dto.guide.GuideCreateRequestDto;
import com.esprit.campconnect.Formation.dto.guide.GuideProgressResponseDto;
import com.esprit.campconnect.Formation.dto.guide.GuideResponseDto;
import com.esprit.campconnect.Formation.dto.guide.GuideStepCreateRequestDto;
import com.esprit.campconnect.Formation.dto.guide.GuideStepResponseDto;
import com.esprit.campconnect.Formation.entity.Formation;
import com.esprit.campconnect.Formation.entity.guide.GuideInteractif;
import com.esprit.campconnect.Formation.repository.guide.GuideInteractifRepository;
import com.esprit.campconnect.Formation.service.FormationService;
import com.esprit.campconnect.Formation.service.guide.GuideInteractifService;
import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/formations/guides-interactifs")
@CrossOrigin(origins = "http://localhost:4200")
public class GuideInteractifController {

    private final GuideInteractifService guideInteractifService;
    private final FormationService formationService;
    private final GuideInteractifRepository guideInteractifRepository;

    public GuideInteractifController(GuideInteractifService guideInteractifService,
                                     FormationService formationService,
                                     GuideInteractifRepository guideInteractifRepository) {
        this.guideInteractifService = guideInteractifService;
        this.formationService = formationService;
        this.guideInteractifRepository = guideInteractifRepository;
    }

    @PostMapping("/formations/{formationId}")
    public GuideResponseDto createGuide(@PathVariable Long formationId,
                                        @Valid @RequestBody GuideCreateRequestDto request,
                                        Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertGuideOrAdminRole(user);

        Formation formation = formationService.getById(formationId);
        assertOwnerOrAdmin(formation.getAuteurEmail(), user);

        return guideInteractifService.createGuide(formationId, request);
    }

    @GetMapping("/formations/{formationId}")
    public GuideResponseDto getByFormation(@PathVariable Long formationId) {
        return guideInteractifService.getGuideByFormation(formationId);
    }

    @PostMapping("/{guideId}/steps")
    public GuideStepResponseDto addStep(@PathVariable Long guideId,
                                        @Valid @RequestBody GuideStepCreateRequestDto request,
                                        Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertGuideOrAdminRole(user);

        GuideInteractif guide = guideInteractifRepository.findById(guideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guide interactif introuvable"));
        assertOwnerOrAdmin(guide.getFormation().getAuteurEmail(), user);

        return guideInteractifService.addStep(guideId, request);
    }

    @GetMapping("/{guideId}/steps")
    public List<GuideStepResponseDto> getSteps(@PathVariable Long guideId) {
        return guideInteractifService.getSteps(guideId);
    }

    @PostMapping("/{guideId}/steps/{stepId}/complete")
    public GuideProgressResponseDto completeStep(@PathVariable Long guideId,
                                                 @PathVariable Long stepId,
                                                 Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        return guideInteractifService.completeStep(guideId, stepId, user.getId());
    }

    @GetMapping("/{guideId}/progress/me")
    public GuideProgressResponseDto myProgress(@PathVariable Long guideId, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        return guideInteractifService.getProgress(guideId, user.getId());
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

    private void assertOwnerOrAdmin(String ownerEmail, Utilisateur user) {
        if (user.getRole() == Role.ADMINISTRATEUR) {
            return;
        }

        if (ownerEmail == null || !ownerEmail.equalsIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action autorisee uniquement pour l'auteur ou un admin");
        }
    }
}
