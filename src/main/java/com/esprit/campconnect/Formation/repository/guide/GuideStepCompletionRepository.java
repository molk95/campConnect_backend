package com.esprit.campconnect.Formation.repository.guide;

import com.esprit.campconnect.Formation.entity.guide.GuideStepCompletion;
import com.esprit.campconnect.Formation.repository.projection.ViewsEvolutionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GuideStepCompletionRepository extends JpaRepository<GuideStepCompletion, Long> {
    boolean existsByProgress_IdAndStep_Id(Long progressId, Long stepId);

    long countByProgress_Id(Long progressId);

    @Query("""
            SELECT FUNCTION('DATE', gsc.completedAt) AS metricDate,
                   COUNT(gsc.id) AS totalCount
            FROM GuideStepCompletion gsc
            GROUP BY FUNCTION('DATE', gsc.completedAt)
            ORDER BY FUNCTION('DATE', gsc.completedAt)
            """)
    List<ViewsEvolutionProjection> aggregateViewsEvolution();

    @Query("""
            SELECT FUNCTION('DATE', gsc.completedAt) AS metricDate,
                   COUNT(gsc.id) AS totalCount
            FROM GuideStepCompletion gsc
            WHERE gsc.progress.guide.formation.id = :formationId
            GROUP BY FUNCTION('DATE', gsc.completedAt)
            ORDER BY FUNCTION('DATE', gsc.completedAt)
            """)
    List<ViewsEvolutionProjection> aggregateViewsEvolutionByFormationId(@Param("formationId") Long formationId);
}
