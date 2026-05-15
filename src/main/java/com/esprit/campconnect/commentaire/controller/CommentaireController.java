
package com.esprit.campconnect.commentaire.controller;

import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.commentaire.entity.Commentaire;
import com.esprit.campconnect.commentaire.service.CommentaireService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/commentaires")
@CrossOrigin(origins = {"http://localhost:4200"})
public class CommentaireController {

    private final CommentaireService commentaireService;


    public CommentaireController(CommentaireService commentaireService) {
        this.commentaireService = commentaireService;
    }

    // Lire les commentaires d'une publication
    @GetMapping("/publication/{publicationId}")
    public List<Commentaire> getByPublication(@PathVariable Long publicationId) {
        return commentaireService.getByPublication(publicationId);

    }

    // N'importe quel utilisateur peut commenter
    @PostMapping("/publication/{publicationId}")
    public Commentaire create(@PathVariable Long publicationId, @RequestBody Commentaire commentaire, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);

        Commentaire toCreate = new Commentaire();
        toCreate.setContenu(commentaire.getContenu());
        toCreate.setAuteurEmail(user.getEmail());
        toCreate.setAuteurNom(user.getNom());

        return commentaireService.create(publicationId, toCreate);
    }

    // N'importe quel utilisateur peut liker un commentaire
    @RequestMapping(value = "/{id}/like", method = {RequestMethod.PUT, RequestMethod.POST})
    public Commentaire like(@PathVariable Long id) {
        return commentaireService.likeCommentaire(id);
    }

    @PutMapping("/{id}")
    public Commentaire update(@PathVariable Long id, @RequestBody Commentaire commentaire, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        Commentaire existing = commentaireService.getById(id);
        assertOwnerOrAdmin(existing.getAuteurEmail(), user);

        Commentaire toUpdate = new Commentaire();
        toUpdate.setContenu(commentaire.getContenu());
        return commentaireService.update(id, toUpdate);
    }

    // Supprimer seulement son propre commentaire
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        Commentaire commentaire = commentaireService.getById(id);
        assertOwnerOrAdmin(commentaire.getAuteurEmail(), user);
        commentaireService.delete(id);
        return ResponseEntity.ok("Commentaire supprime.");
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

