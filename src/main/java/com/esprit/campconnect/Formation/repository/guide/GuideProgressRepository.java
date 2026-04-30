package com.esprit.campconnect.Formation.repository.guide;

import com.esprit.campconnect.Formation.entity.guide.GuideProgress;
import com.esprit.campconnect.Formation.repository.projection.FormationCountProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuideProgressRepository extends JpaRepository<GuideProgress, Long> {
    Optional<GuideProgress> findByGuide_IdAndUtilisateur_Id(Long guideId, Long utilisateurId);

    long countByCompletedTrue();

    @Query("""
            SELECT COUNT(gp)
            FROM GuideProgress gp
            WHERE gp.completed = false AND gp.completedSteps > 0
            """)
    long countInProgress();

    long countByGuide_Id(Long guideId);

    long countByGuide_IdAndCompletedTrue(Long guideId);

    long countDistinctUtilisateur_IdByGuide_Id(Long guideId);

    @Query("""
            SELECT COALESCE(AVG(gp.progressPercent), 0D)
            FROM GuideProgress gp
            WHERE gp.guide.id = :guideId
            """)
    double averageProgressByGuideId(@Param("guideId") Long guideId);

    @Query("""
            SELECT gp.guide.formation.id AS formationId,
                   gp.guide.formation.titre AS titre,
                   COUNT(DISTINCT gp.utilisateur.id) AS totalCount
            FROM GuideProgress gp
            GROUP BY gp.guide.formation.id, gp.guide.formation.titre, gp.guide.formation.dateCreation
            ORDER BY COUNT(DISTINCT gp.utilisateur.id) DESC, gp.guide.formation.dateCreation DESC
            """)
    List<FormationCountProjection> findTopViewedFormations(Pageable pageable);
}
