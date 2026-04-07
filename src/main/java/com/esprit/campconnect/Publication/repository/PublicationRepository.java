package com.esprit.campconnect.Publication.repository;

import com.esprit.campconnect.Publication.entity.Publication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
}