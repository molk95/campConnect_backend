package com.esprit.campconnect.Formation.service.stats;

import com.esprit.campconnect.Formation.dto.stats.FormationDetailsStatsDto;
import com.esprit.campconnect.Formation.dto.stats.FormationGlobalStatsDto;

public interface FormationStatsService {
    FormationGlobalStatsDto getGlobalStats();

    FormationDetailsStatsDto getFormationStats(Long formationId);
}
