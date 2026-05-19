package com.esprit.campconnect.Formation.repository.guide;

import com.esprit.campconnect.Formation.entity.guide.GuideInteractif;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuideInteractifRepository extends JpaRepository<GuideInteractif, Long> {
    Optional<GuideInteractif> findByFormation_Id(Long formationId);
}
