package com.esprit.campconnect.Formation.repository;

import com.esprit.campconnect.Formation.entity.FormationMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FormationMediaRepository extends JpaRepository<FormationMedia, Long> {

    List<FormationMedia> findByFormation_IdOrderByDisplayOrderAscIdAsc(Long formationId);

    Optional<FormationMedia> findByIdAndFormation_Id(Long id, Long formationId);

    @Query("SELECT MAX(fm.displayOrder) FROM FormationMedia fm WHERE fm.formation.id = :formationId")
    Integer getMaxDisplayOrderForFormation(@Param("formationId") Long formationId);
}
