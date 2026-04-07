package com.esprit.campconnect.Publication.service;

import com.esprit.campconnect.Publication.entity.Publication;

import java.util.List;

public interface PublicationService {
    List<Publication> getAllPublications();
    Publication getPublicationById(Long id);
    Publication createPublication(Publication publication);
    Publication updatePublication(Long id, Publication publication);
    void deletePublication(Long id);
}