package com.esprit.campconnect.Formation.repository.guide;

import com.esprit.campconnect.Formation.entity.guide.GuideStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuideStepRepository extends JpaRepository<GuideStep, Long> {
    List<GuideStep> findByGuide_IdOrderByStepOrderAsc(Long guideId);

    Optional<GuideStep> findByIdAndGuide_Id(Long stepId, Long guideId);

    boolean existsByGuide_IdAndStepOrder(Long guideId, Integer stepOrder);
}
