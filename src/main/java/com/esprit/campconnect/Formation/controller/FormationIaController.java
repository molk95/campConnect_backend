package com.esprit.campconnect.Formation.controller;

import com.esprit.campconnect.Formation.dto.ai.FormationAiGenerateRequestDto;
import com.esprit.campconnect.Formation.dto.ai.FormationAiGenerateResponseDto;
import com.esprit.campconnect.Formation.service.ia.FormationIaService;
import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/formations/ia")
@CrossOrigin(origins = "http://localhost:4200")
public class FormationIaController {

    private final FormationIaService formationIaService;

    public FormationIaController(FormationIaService formationIaService) {
        this.formationIaService = formationIaService;
    }

    @PostMapping("/generate")
    public FormationAiGenerateResponseDto generate(@Valid @RequestBody FormationAiGenerateRequestDto request,
                                                   Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertGuideOrAdminRole(user);
        return formationIaService.generateContent(request);
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
}
