package com.esprit.campconnect.Formation.repository;

import com.esprit.campconnect.Formation.entity.FormationLike;
import com.esprit.campconnect.Formation.repository.projection.FormationCountProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FormationLikeRepository extends JpaRepository<FormationLike, Long> {

    interface FormationLikeCountProjection {
        Long getFormationId();

        Long getLikeCount();
    }

    boolean existsByFormation_IdAndUtilisateur_Id(Long formationId, Long utilisateurId);

    long countByFormation_Id(Long formationId);

    long deleteByFormation_IdAndUtilisateur_Id(Long formationId, Long utilisateurId);

    @Query("""
            SELECT fl.formation.id AS formationId, COUNT(fl.id) AS likeCount
            FROM FormationLike fl
            WHERE fl.formation.id IN :formationIds
            GROUP BY fl.formation.id
            """)
    List<FormationLikeCountProjection> countLikesByFormationIds(@Param("formationIds") List<Long> formationIds);

    @Query("""
            SELECT fl.formation.id
            FROM FormationLike fl
            WHERE fl.utilisateur.id = :userId
              AND fl.formation.id IN :formationIds
            """)
    List<Long> findLikedFormationIdsByUserAndFormationIds(@Param("userId") Long userId,
                                                           @Param("formationIds") List<Long> formationIds);

    @Query("""
            SELECT fl.formation.id AS formationId,
                   fl.formation.titre AS titre,
                   COUNT(fl.id) AS totalCount
            FROM FormationLike fl
            GROUP BY fl.formation.id, fl.formation.titre, fl.formation.dateCreation
            ORDER BY COUNT(fl.id) DESC, fl.formation.dateCreation DESC
            """)
    List<FormationCountProjection> findTopLikedFormations(Pageable pageable);
}
