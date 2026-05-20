package com.esprit.campconnect.Formation.controller;

import com.esprit.campconnect.Formation.dto.FormationMediaResponseDto;
import com.esprit.campconnect.Formation.entity.Formation;
import com.esprit.campconnect.Formation.service.FormationMediaService;
import com.esprit.campconnect.Formation.service.FormationService;
import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/formations")
@CrossOrigin(origins = "http://localhost:4200")
public class FormationMediaController {

    private final FormationMediaService formationMediaService;
    private final FormationService formationService;

    public FormationMediaController(FormationMediaService formationMediaService, FormationService formationService) {
        this.formationMediaService = formationMediaService;
        this.formationService = formationService;
    }

    @PostMapping(value = "/{formationId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FormationMediaResponseDto> uploadMedia(@PathVariable Long formationId,
                                                                 @RequestPart("file") MultipartFile file,
                                                                 Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertGuideOrAdminRole(user);

        Formation formation = formationService.getById(formationId);
        assertOwnerOrAdmin(formation.getAuteurEmail(), user);

        FormationMediaResponseDto created = formationMediaService.addMedia(formationId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{formationId}/media")
    public List<FormationMediaResponseDto> getFormationMedia(@PathVariable Long formationId) {
        return formationMediaService.getMediaByFormation(formationId);
    }

    @DeleteMapping("/{formationId}/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long formationId,
                                            @PathVariable Long mediaId,
                                            Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertGuideOrAdminRole(user);

        Formation formation = formationService.getById(formationId);
        assertOwnerOrAdmin(formation.getAuteurEmail(), user);

        formationMediaService.deleteMedia(formationId, mediaId);
        return ResponseEntity.noContent().build();
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
