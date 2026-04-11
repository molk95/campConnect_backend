
package com.esprit.campconnect.Publication.repository;

import com.esprit.campconnect.Publication.entity.Publication;
import com.esprit.campconnect.commentaire.entity.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicationRepository extends JpaRepository<Publication, Long> {
    List<Publication> findByForum_Id(Long forumId);
    List<Publication> findByAuteurEmail(String auteurEmail);
    List<Commentaire> findByPublication_Id(Long publicationId);

}