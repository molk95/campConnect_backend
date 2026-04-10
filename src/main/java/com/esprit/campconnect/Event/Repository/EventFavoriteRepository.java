package com.esprit.campconnect.Event.Repository;

import com.esprit.campconnect.Event.Entity.EventFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventFavoriteRepository extends JpaRepository<EventFavorite, Long> {

    boolean existsByUtilisateurIdAndEventId(Long utilisateurId, Long eventId);

    Optional<EventFavorite> findByUtilisateurIdAndEventId(Long utilisateurId, Long eventId);

    List<EventFavorite> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);

    long countByEventId(Long eventId);

    void deleteByUtilisateurIdAndEventId(Long utilisateurId, Long eventId);
}
