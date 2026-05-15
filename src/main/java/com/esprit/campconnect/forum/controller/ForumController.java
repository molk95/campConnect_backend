package com.esprit.campconnect.forum.controller;

import com.esprit.campconnect.forum.DTO.ForumRequestDto;
import com.esprit.campconnect.forum.entity.Forum;
import com.esprit.campconnect.forum.service.ForumService;
import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/forums")

@CrossOrigin(origins = "http://localhost:4200")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @GetMapping
    public List<Forum> getAll() {
        return forumService.getAll();
    }

    @GetMapping("/{id}")
    public Forum getById(@PathVariable Long id) {
        Forum forum = forumService.getById(id);

        // Eviter boucle JSON / lazy loading
        forum.setPublications(new ArrayList<>());

        return forum;
    }

    @PostMapping
    public Forum create(@RequestBody ForumRequestDto dto, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);

        Forum forum = new Forum();
        forum.setNom(dto.getNom());
        forum.setDescription(dto.getDescription());
        forum.setCategorie(dto.getCategorie());
        forum.setIcon(dto.getIcon());
        forum.setAuteurEmail(user.getEmail());
        forum.setAuteurNom(user.getNom());

        return forumService.create(forum);
    }

    @PutMapping("/{id}")
    public Forum update(@PathVariable Long id, @RequestBody ForumRequestDto dto, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        Forum existing = forumService.getById(id);
        assertOwnerOrAdmin(existing.getAuteurEmail(), user);

        Forum forum = new Forum();
        forum.setNom(dto.getNom());
        forum.setDescription(dto.getDescription());
        forum.setCategorie(dto.getCategorie());
        forum.setIcon(dto.getIcon());

        return forumService.update(id, forum);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        Forum existing = forumService.getById(id);
        assertOwnerOrAdmin(existing.getAuteurEmail(), user);
        forumService.delete(id);
    }

    private Utilisateur requireAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Utilisateur user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        return user;
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
