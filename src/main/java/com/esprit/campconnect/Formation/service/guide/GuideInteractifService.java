package com.esprit.campconnect.Formation.service.guide;

import com.esprit.campconnect.Formation.dto.guide.GuideCreateRequestDto;
import com.esprit.campconnect.Formation.dto.guide.GuideProgressResponseDto;
import com.esprit.campconnect.Formation.dto.guide.GuideResponseDto;
import com.esprit.campconnect.Formation.dto.guide.GuideStepCreateRequestDto;
import com.esprit.campconnect.Formation.dto.guide.GuideStepResponseDto;

import java.util.List;

public interface GuideInteractifService {
    GuideResponseDto createGuide(Long formationId, GuideCreateRequestDto request);

    GuideResponseDto getGuideByFormation(Long formationId);

    GuideStepResponseDto addStep(Long guideId, GuideStepCreateRequestDto request);

    List<GuideStepResponseDto> getSteps(Long guideId);

    GuideProgressResponseDto completeStep(Long guideId, Long stepId, Long utilisateurId);

    GuideProgressResponseDto getProgress(Long guideId, Long utilisateurId);
}
