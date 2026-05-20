package com.esprit.campconnect.Formation.service.guide;

import com.esprit.campconnect.Formation.dto.guide.GuideCreateRequestDto;
import com.esprit.campconnect.Formation.dto.guide.GuideProgressResponseDto;
import com.esprit.campconnect.Formation.dto.guide.GuideQuizResponseDto;
import com.esprit.campconnect.Formation.dto.guide.GuideQuizSubmitRequestDto;
import com.esprit.campconnect.Formation.dto.guide.GuideQuizUpsertRequestDto;
import com.esprit.campconnect.Formation.dto.guide.GuideResponseDto;
import com.esprit.campconnect.Formation.dto.guide.GuideStepCreateRequestDto;
import com.esprit.campconnect.Formation.dto.guide.GuideStepResponseDto;

import java.util.List;

public interface GuideInteractifService {
    GuideResponseDto createGuide(Long formationId, GuideCreateRequestDto request);

    GuideResponseDto getGuideByFormation(Long formationId);

    GuideResponseDto updateGuideByFormation(Long formationId, GuideCreateRequestDto request);

    GuideStepResponseDto addStep(Long guideId, GuideStepCreateRequestDto request);

    GuideStepResponseDto updateStep(Long guideId, Long stepId, GuideStepCreateRequestDto request);

    void deleteStep(Long guideId, Long stepId);

    List<GuideStepResponseDto> getSteps(Long guideId);

    GuideProgressResponseDto startGuide(Long guideId, Long utilisateurId);

    GuideProgressResponseDto completeStep(Long guideId, Long stepId, Long utilisateurId);

    GuideProgressResponseDto getProgress(Long guideId, Long utilisateurId);

    GuideQuizResponseDto upsertFormationQuiz(Long formationId, GuideQuizUpsertRequestDto request);

    GuideQuizResponseDto getFormationQuiz(Long formationId, boolean includeAnswers);

    GuideProgressResponseDto submitQuiz(Long guideId, Long utilisateurId, GuideQuizSubmitRequestDto request);
}
