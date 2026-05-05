package com.esprit.campconnect.Publication.controller;

import com.esprit.campconnect.Publication.entity.Publication;
import com.esprit.campconnect.Publication.repository.PublicationRepository;
import com.esprit.campconnect.Publication.service.PublicationService;
import com.esprit.campconnect.commentaire.entity.Commentaire;
import com.esprit.campconnect.commentaire.repository.CommentaireRepository;
import com.esprit.campconnect.forum.entity.Forum;
import com.esprit.campconnect.forum.repository.ForumRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/create")
    @Transactional
    public Publication createPublication(@RequestBody Publication publication) {

        System.out.println("Publication reçue = " + publication.getTitre());

        if (publication.getForum() == null || publication.getForum().getId() == null) {
            throw new RuntimeException("Forum ID manquant !");
        }

        Forum forum = forumRepository.findById(publication.getForum().getId())
                .orElseThrow(() -> new RuntimeException("Forum introuvable"));

        publication.setForum(forum);

        return publicationRepository.save(publication);
    }

    @PutMapping("/{id}/like")
    @Transactional
    public Publication likePublication(@PathVariable Long id) {

        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication introuvable"));

        publication.setLikesCount(publication.getLikesCount() + 1);

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
    public Commentaire createCommentaire(@RequestBody Commentaire commentaire) {

        if (commentaire.getPublication() == null || commentaire.getPublication().getId() == null) {
            throw new RuntimeException("Publication ID manquant !");
        }

        Publication pub = publicationRepository.findById(commentaire.getPublication().getId())
                .orElseThrow(() -> new RuntimeException("Publication introuvable"));

        commentaire.setPublication(pub);

        return commentaireRepository.save(commentaire);
    }
}