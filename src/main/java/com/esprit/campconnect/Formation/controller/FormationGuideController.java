package com.esprit.campconnect.Formation.controller;

import com.esprit.campconnect.Formation.dto.guide.GuideCreateRequestDto;
import com.esprit.campconnect.Formation.dto.guide.GuideProgressResponseDto;
import com.esprit.campconnect.Formation.dto.guide.GuideResponseDto;
import com.esprit.campconnect.Formation.entity.Formation;
import com.esprit.campconnect.Formation.entity.guide.GuideInteractif;
import com.esprit.campconnect.Formation.entity.guide.GuideStep;
import com.esprit.campconnect.Formation.repository.guide.GuideInteractifRepository;
import com.esprit.campconnect.Formation.repository.guide.GuideStepRepository;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/formations")
@CrossOrigin(origins = "http://localhost:4200")
public class FormationGuideController {

    private final GuideInteractifService guideInteractifService;
    private final GuideInteractifRepository guideInteractifRepository;
    private final GuideStepRepository guideStepRepository;
    private final FormationService formationService;

    public FormationGuideController(GuideInteractifService guideInteractifService,
                                    GuideInteractifRepository guideInteractifRepository,
                                    GuideStepRepository guideStepRepository,
                                    FormationService formationService) {
        this.guideInteractifService = guideInteractifService;
        this.guideInteractifRepository = guideInteractifRepository;
        this.guideStepRepository = guideStepRepository;
        this.formationService = formationService;
    }

    @GetMapping("/{formationId:\\d+}/guide")
    public GuideResponseDto getGuide(@PathVariable Long formationId) {
        return guideInteractifService.getGuideByFormation(formationId);
    }

    @PostMapping("/{formationId:\\d+}/guide")
    public GuideResponseDto createGuide(@PathVariable Long formationId,
                                        @Valid @RequestBody GuideCreateRequestDto request,
                                        Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertGuideOrAdminRole(user);

        Formation formation = formationService.getById(formationId);
        assertOwnerOrAdmin(formation.getAuteurEmail(), user);
        return guideInteractifService.createGuide(formationId, request);
    }

    @PutMapping("/{formationId:\\d+}/guide")
    public GuideResponseDto updateGuide(@PathVariable Long formationId,
                                        @Valid @RequestBody GuideCreateRequestDto request,
                                        Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertGuideOrAdminRole(user);

        Formation formation = formationService.getById(formationId);
        assertOwnerOrAdmin(formation.getAuteurEmail(), user);
        return guideInteractifService.updateGuideByFormation(formationId, request);
    }

    @GetMapping("/{formationId:\\d+}/guide/progress")
    public GuideProgressResponseDto getGuideProgress(@PathVariable Long formationId, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        GuideInteractif guide = getGuideByFormationOr404(formationId);
        return guideInteractifService.getProgress(guide.getId(), user.getId());
    }

    @PostMapping("/{formationId:\\d+}/guide/progress")
    public GuideProgressResponseDto startGuideProgress(@PathVariable Long formationId, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        GuideInteractif guide = getGuideByFormationOr404(formationId);
        return guideInteractifService.startGuide(guide.getId(), user.getId());
    }

    @PostMapping("/{formationId:\\d+}/guide/steps/{stepOrder}/complete")
    public GuideProgressResponseDto completeStepByOrder(@PathVariable Long formationId,
                                                        @PathVariable Integer stepOrder,
                                                        Authentication authentication) {
        if (stepOrder == null || stepOrder <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "stepOrder doit etre > 0");
        }

        Utilisateur user = requireAuthenticatedUser(authentication);
        GuideInteractif guide = getGuideByFormationOr404(formationId);
        GuideStep step = guideStepRepository.findByGuide_IdAndStepOrder(guide.getId(), stepOrder)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Etape introuvable pour ce guide"));

        return guideInteractifService.completeStep(guide.getId(), step.getId(), user.getId());
    }

    private GuideInteractif getGuideByFormationOr404(Long formationId) {
        return guideInteractifRepository.findByFormation_Id(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guide interactif introuvable pour cette formation"));
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
