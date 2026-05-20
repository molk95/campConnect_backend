package com.esprit.campconnect.Formation.repository;

import com.esprit.campconnect.Formation.entity.FormationQuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FormationQuizQuestionRepository extends JpaRepository<FormationQuizQuestion, Long> {

    List<FormationQuizQuestion> findByFormation_IdOrderByQuestionOrderAsc(Long formationId);

    void deleteByFormation_Id(Long formationId);

    long countByFormation_Id(Long formationId);

    @Query("""
            SELECT COALESCE(AVG(gp.quizScore), 0D)
            FROM GuideProgress gp
            WHERE gp.guide.formation.id = :formationId
              AND gp.quizScore IS NOT NULL
            """)
    double averageQuizScoreByFormationId(@Param("formationId") Long formationId);

    @Query("""
            SELECT COUNT(gp)
            FROM GuideProgress gp
            WHERE gp.guide.formation.id = :formationId
              AND gp.quizPassed = true
            """)
    long countQuizPassedByFormationId(@Param("formationId") Long formationId);

    @Query("""
            SELECT COUNT(gp)
            FROM GuideProgress gp
            WHERE gp.guide.formation.id = :formationId
              AND gp.quizScore IS NOT NULL
            """)
    long countQuizAttemptedByFormationId(@Param("formationId") Long formationId);

    Optional<FormationQuizQuestion> findByFormation_IdAndQuestionOrder(Long formationId, Integer questionOrder);
}
