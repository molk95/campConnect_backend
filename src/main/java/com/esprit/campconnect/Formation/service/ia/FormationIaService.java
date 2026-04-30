package com.esprit.campconnect.Formation.service.ia;

import com.esprit.campconnect.Formation.dto.ai.FormationAiGenerateRequestDto;
import com.esprit.campconnect.Formation.dto.ai.FormationAiGenerateResponseDto;

public interface FormationIaService {
    FormationAiGenerateResponseDto generateContent(FormationAiGenerateRequestDto request);
}
