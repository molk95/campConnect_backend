package com.esprit.campconnect.Formation.service.stats;

import com.esprit.campconnect.Formation.dto.stats.FormationDetailsStatsDto;
import com.esprit.campconnect.Formation.dto.stats.FormationGlobalStatsDto;
import com.esprit.campconnect.Formation.dto.stats.FormationStatsTopItemDto;
import com.esprit.campconnect.Formation.dto.stats.FormationViewsEvolutionPointDto;
import com.esprit.campconnect.Formation.dto.stats.GuideProgressDistributionDto;
import com.esprit.campconnect.Formation.entity.Formation;
import com.esprit.campconnect.Formation.entity.FormationStatus;
import com.esprit.campconnect.Formation.entity.guide.GuideInteractif;
import com.esprit.campconnect.Formation.repository.FormationLikeRepository;
import com.esprit.campconnect.Formation.repository.FormationRepository;
import com.esprit.campconnect.Formation.repository.guide.GuideInteractifRepository;
import com.esprit.campconnect.Formation.repository.guide.GuideProgressRepository;
import com.esprit.campconnect.Formation.repository.guide.GuideStepCompletionRepository;
import com.esprit.campconnect.Formation.repository.projection.FormationCountProjection;
import com.esprit.campconnect.Formation.repository.projection.ViewsEvolutionProjection;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FormationStatsServiceImpl implements FormationStatsService {

    private static final int TOP_FORMATIONS_LIMIT = 5;

    private final FormationRepository formationRepository;
    private final FormationLikeRepository formationLikeRepository;
    private final GuideInteractifRepository guideInteractifRepository;
    private final GuideProgressRepository guideProgressRepository;
    private final GuideStepCompletionRepository guideStepCompletionRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(readOnly = true)
    public FormationGlobalStatsDto getGlobalStats() {
        long totalFormations = formationRepository.count();
        long totalUsers = utilisateurRepository.count();
        long totalLikes = formationLikeRepository.count();
        long totalPublished = formationRepository.countByStatus(FormationStatus.PUBLISHED);
        long totalDrafts = formationRepository.countByStatus(FormationStatus.DRAFT);

        long guideCompletedCount = guideProgressRepository.countByCompletedTrue();
        long guideInProgressCount = guideProgressRepository.countInProgress();
        long totalGuides = guideInteractifRepository.count();
        long guideNotStartedCount = Math.max(
                0L,
                safeMultiply(totalUsers, totalGuides) - guideCompletedCount - guideInProgressCount
        );

        FormationGlobalStatsDto dto = new FormationGlobalStatsDto();
        dto.setTotalFormations(totalFormations);
        dto.setTotalUsers(totalUsers);
        dto.setTotalLikes(totalLikes);
        dto.setTotalPublished(totalPublished);
        dto.setTotalDrafts(totalDrafts);
        dto.setGuideCompletedCount(guideCompletedCount);
        dto.setGuideInProgressCount(guideInProgressCount);
        dto.setGuideNotStartedCount(guideNotStartedCount);

        GuideProgressDistributionDto distribution = new GuideProgressDistributionDto();
        distribution.setCompleted(guideCompletedCount);
        distribution.setInProgress(guideInProgressCount);
        distribution.setNotStarted(guideNotStartedCount);
        dto.setGuideProgressDistribution(distribution);

        dto.setTopViewedFormations(
                mapTopItems(guideProgressRepository.findTopViewedFormations(PageRequest.of(0, TOP_FORMATIONS_LIMIT)))
        );
        dto.setTopLikedFormations(
                mapTopItems(formationLikeRepository.findTopLikedFormations(PageRequest.of(0, TOP_FORMATIONS_LIMIT)))
        );
        dto.setViewsEvolution(mapViewsEvolution(guideStepCompletionRepository.aggregateViewsEvolution()));

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public FormationDetailsStatsDto getFormationStats(Long formationId) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation introuvable"));

        long totalUsers = utilisateurRepository.count();
        long likesCount = formationLikeRepository.countByFormation_Id(formationId);

        FormationDetailsStatsDto dto = new FormationDetailsStatsDto();
        dto.setFormationId(formation.getId());
        dto.setTitle(formation.getTitre());
        dto.setLikesCount(likesCount);
        dto.setAverageQuizScore(0D);

        Optional<GuideInteractif> guideOpt = guideInteractifRepository.findByFormation_Id(formationId);
        if (guideOpt.isEmpty()) {
            dto.setViewsCount(0L);
            dto.setCompletionRate(0D);
            dto.setAverageProgress(0D);
            dto.setViewsEvolution(List.of());
            return dto;
        }

        Long guideId = guideOpt.get().getId();
        long viewsCount = guideProgressRepository.countDistinctUtilisateur_IdByGuide_Id(guideId);
        long completedCount = guideProgressRepository.countByGuide_IdAndCompletedTrue(guideId);
        long startedCount = guideProgressRepository.countByGuide_Id(guideId);
        double startedAverageProgress = guideProgressRepository.averageProgressByGuideId(guideId);

        double completionRate = totalUsers == 0
                ? 0D
                : roundTwoDecimals((completedCount * 100.0) / totalUsers);

        double averageProgress = totalUsers == 0
                ? 0D
                : roundTwoDecimals((startedAverageProgress * startedCount) / totalUsers);

        dto.setViewsCount(viewsCount);
        dto.setCompletionRate(completionRate);
        dto.setAverageProgress(averageProgress);
        dto.setViewsEvolution(mapViewsEvolution(
                guideStepCompletionRepository.aggregateViewsEvolutionByFormationId(formationId)
        ));

        return dto;
    }

    private List<FormationStatsTopItemDto> mapTopItems(List<FormationCountProjection> projections) {
        return projections.stream().map(this::toTopItem).toList();
    }

    private FormationStatsTopItemDto toTopItem(FormationCountProjection projection) {
        FormationStatsTopItemDto item = new FormationStatsTopItemDto();
        item.setFormationId(projection.getFormationId());
        item.setTitle(projection.getTitre());
        item.setCount(projection.getTotalCount() != null ? projection.getTotalCount() : 0L);
        return item;
    }

    private List<FormationViewsEvolutionPointDto> mapViewsEvolution(List<ViewsEvolutionProjection> rows) {
        return rows.stream().map(row -> {
            FormationViewsEvolutionPointDto point = new FormationViewsEvolutionPointDto();
            point.setDate(row.getMetricDate());
            point.setViewsCount(row.getTotalCount() != null ? row.getTotalCount() : 0L);
            return point;
        }).toList();
    }

    private long safeMultiply(long first, long second) {
        if (first <= 0 || second <= 0) {
            return 0L;
        }

        if (first > Long.MAX_VALUE / second) {
            return Long.MAX_VALUE;
        }

        return first * second;
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
