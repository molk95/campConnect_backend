package com.esprit.campconnect.Publication.service;

import com.esprit.campconnect.Publication.entity.Publication;
import com.esprit.campconnect.Publication.repository.PublicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicationServiceImpl implements PublicationService {

    private final PublicationRepository publicationRepository;

    public PublicationServiceImpl(PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    @Override
    public List<Publication> getAllPublications() {
        return publicationRepository.findAll();
    }

    @Override
    public Publication getPublicationById(Long id) {
        return publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication introuvable avec id : " + id));
    }

    @Override
    public Publication createPublication(Publication publication) {
        return publicationRepository.save(publication);
    }

    @Override
    public Publication updatePublication(Long id, Publication publication) {
        Publication existing = getPublicationById(id);
        existing.setContenu(publication.getContenu());
        return publicationRepository.save(existing);
    }

    @Override
    public void deletePublication(Long id) {
        Publication publication = getPublicationById(id);
        publicationRepository.delete(publication);
    }
}