package com.esprit.campconnect.Publication.controller;

import com.esprit.campconnect.Publication.DTO.PublicationRequestDto;
import com.esprit.campconnect.Publication.entity.Publication;
import com.esprit.campconnect.Publication.repository.PublicationRepository;
import com.esprit.campconnect.Publication.service.PublicationService;
import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.commentaire.entity.Commentaire;
import com.esprit.campconnect.commentaire.repository.CommentaireRepository;
import com.esprit.campconnect.forum.entity.Forum;
import com.esprit.campconnect.forum.repository.ForumRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/publications")
@CrossOrigin(origins = "http://localhost:4200")
@Transactional(readOnly = true)
public class PublicationController {

    private final PublicationRepository publicationRepository;
    private final ForumRepository forumRepository;
    private final PublicationService publicationService;
    private final CommentaireRepository commentaireRepository;

    // ✅ Constructor injection complet
    public PublicationController(PublicationRepository publicationRepository,
                                 ForumRepository forumRepository,
                                 PublicationService publicationService,
                                 CommentaireRepository commentaireRepository) {

        this.publicationRepository = publicationRepository;
        this.forumRepository = forumRepository;
        this.publicationService = publicationService;
        this.commentaireRepository = commentaireRepository;
    }

    // =========================
    // PUBLICATIONS
    // =========================

    @GetMapping
    public List<Publication> getAll() {
        return publicationService.getAllPublications();
    }

    @GetMapping("/forum/{forumId}")
    public List<Publication> getByForum(@PathVariable Long forumId) {
        return publicationService.getByForumId(forumId);
    }

    @GetMapping("/{id}")
    public Publication getById(@PathVariable Long id) {
        return publicationService.getPublicationById(id);
    }

    @PostMapping({"", "/create"})
    @Transactional
    public Publication createPublication(@RequestBody PublicationRequestDto request, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);

        Long forumId = request.resolveForumId();
        if (forumId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Forum ID manquant");
        }
        if (request.getTitre() == null || request.getTitre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Titre manquant");
        }
        if (request.getContenu() == null || request.getContenu().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contenu manquant");
        }

        Forum forum = forumRepository.findById(forumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Forum introuvable"));

        Publication toCreate = new Publication();
        toCreate.setTitre(request.getTitre().trim());
        toCreate.setContenu(request.getContenu().trim());
        toCreate.setForum(forum);
        toCreate.setAuteurEmail(user.getEmail());
        toCreate.setAuteurNom(user.getNom());

        return publicationRepository.save(toCreate);
    }

    @PutMapping("/{id}")
    @Transactional
    public Publication updatePublication(@PathVariable Long id, @RequestBody Publication publication, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);

        Publication existing = publicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publication introuvable"));

        assertOwnerOrAdmin(existing.getAuteurEmail(), user);

        existing.setTitre(publication.getTitre());
        existing.setContenu(publication.getContenu());

        return publicationRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void deletePublication(@PathVariable Long id, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);

        Publication existing = publicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publication introuvable"));

        assertOwnerOrAdmin(existing.getAuteurEmail(), user);
        publicationRepository.delete(existing);
    }

    @RequestMapping(value = "/{id}/like", method = {RequestMethod.PUT, RequestMethod.POST})
    @Transactional
    public Publication likePublication(@PathVariable Long id) {

        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication introuvable"));

        int currentLikes = publication.getLikesCount() == null ? 0 : publication.getLikesCount();
        publication.setLikesCount(currentLikes + 1);

        return publicationRepository.save(publication);
    }

    @PutMapping("/{id}/view")
    @Transactional
    public Publication incrementView(@PathVariable Long id) {

        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication introuvable"));

        publication.setVuesCount(publication.getVuesCount() + 1);

        return publicationRepository.save(publication);
    }

    // =========================
    // COMMENTAIRES
    // =========================

    @GetMapping("/{id}/commentaires")
    public List<Commentaire> getCommentairesByPublication(@PathVariable Long id) {
        return commentaireRepository.findByPublication_Id(id);
    }

    @PostMapping("/commentaire/create")
    @Transactional
    public Commentaire createCommentaire(@RequestBody Commentaire commentaire, Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);

        if (commentaire.getPublication() == null || commentaire.getPublication().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Publication ID manquant");
        }

        Publication pub = publicationRepository.findById(commentaire.getPublication().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publication introuvable"));

        Commentaire toCreate = new Commentaire();
        toCreate.setContenu(commentaire.getContenu());
        toCreate.setPublication(pub);
        toCreate.setAuteurEmail(user.getEmail());
        toCreate.setAuteurNom(user.getNom());

        return commentaireRepository.save(toCreate);
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
