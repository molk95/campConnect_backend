package com.esprit.campconnect.Formation.repository;

import com.esprit.campconnect.Formation.entity.Formation;
import com.esprit.campconnect.Formation.entity.FormationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FormationRepository extends JpaRepository<Formation, Long> {

    List<Formation> findAllByOrderByDateCreationDesc();

    List<Formation> findByGuide_IdOrderByDateCreationDesc(Long guideId);

    long countByStatus(FormationStatus status);

    @Query("""
            SELECT f
            FROM Formation f
            WHERE (:query IS NULL
                OR LOWER(f.titre) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(f.description) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:guideId IS NULL OR f.guide.id = :guideId)
              AND (:status IS NULL OR f.status = :status)
              AND (:dateFrom IS NULL OR f.dateCreation >= :dateFrom)
              AND (:dateTo IS NULL OR f.dateCreation <= :dateTo)
            """)
    Page<Formation> search(
            @Param("query") String query,
            @Param("guideId") Long guideId,
            @Param("status") FormationStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );
}
