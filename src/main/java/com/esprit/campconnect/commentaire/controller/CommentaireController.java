package com.esprit.campconnect.commentaire.controller;

import com.esprit.campconnect.commentaire.entity.Commentaire;
import com.esprit.campconnect.commentaire.service.CommentaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commentaires")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentaireController {

    private final CommentaireService commentaireService;

    @GetMapping("/publication/{publicationId}")
    public List<Commentaire> getByPublication(@PathVariable Long publicationId) {
        return commentaireService.getByPublication(publicationId);
    }

    @PostMapping("/publication/{publicationId}")
    public Commentaire create(@PathVariable Long publicationId, @RequestBody Commentaire commentaire) {
        return commentaireService.create(publicationId, commentaire);
    }

    @PutMapping("/{id}")
    public Commentaire update(@PathVariable Long id, @RequestBody Commentaire commentaire) {
        return commentaireService.update(id, commentaire);
    }

    @PutMapping("/{id}/like")
    public Commentaire like(@PathVariable Long id) {
        return commentaireService.likeCommentaire(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        commentaireService.delete(id);
    }
}