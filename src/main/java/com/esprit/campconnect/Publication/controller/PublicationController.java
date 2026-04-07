package com.esprit.campconnect.Publication.controller;
import com.esprit.campconnect.Publication.entity.Publication;
import com.esprit.campconnect.Publication.service.PublicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/publications")
@CrossOrigin(origins = {"http://localhost:4200"})
public class PublicationController {

    @Autowired
    private PublicationService publicationService;

    @PostMapping
    public Publication createPublication(@RequestBody Publication publication) {
        return publicationService.createPublication(publication);
    }

    @GetMapping
    public List<Publication> agetAllPublications() {
        return publicationService.getAllPublications();
    }

    @GetMapping("/{id}")
    public Publication getPublicationById(@PathVariable Long id) {
        return publicationService.getPublicationById(id);
    }

    @PutMapping("/{id}")
    public Publication updatePublication(@PathVariable Long id, @RequestBody Publication publication) {
        return publicationService.updatePublication(id, publication);
    }

    @DeleteMapping("/{id}")
    public void deletePublication(@PathVariable Long id) {
        publicationService.deletePublication(id);
    }
}