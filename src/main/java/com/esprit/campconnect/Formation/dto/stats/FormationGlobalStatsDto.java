package com.esprit.campconnect.Formation.dto.stats;

import java.util.ArrayList;
import java.util.List;

public class FormationGlobalStatsDto {

    private long totalFormations;
    private long totalUsers;
    private long totalLikes;
    private long totalPublished;
    private long totalDrafts;
    private long guideCompletedCount;
    private long guideInProgressCount;
    private long guideNotStartedCount;

    private List<FormationStatsTopItemDto> topViewedFormations = new ArrayList<>();
    private List<FormationStatsTopItemDto> topLikedFormations = new ArrayList<>();
    private GuideProgressDistributionDto guideProgressDistribution = new GuideProgressDistributionDto();
    private List<FormationViewsEvolutionPointDto> viewsEvolution = new ArrayList<>();

    public long getTotalFormations() {
        return totalFormations;
    }

    public void setTotalFormations(long totalFormations) {
        this.totalFormations = totalFormations;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public long getTotalPublished() {
        return totalPublished;
    }

    public void setTotalPublished(long totalPublished) {
        this.totalPublished = totalPublished;
    }

    public long getTotalDrafts() {
        return totalDrafts;
    }

    public void setTotalDrafts(long totalDrafts) {
        this.totalDrafts = totalDrafts;
    }

    public long getGuideCompletedCount() {
        return guideCompletedCount;
    }

    public void setGuideCompletedCount(long guideCompletedCount) {
        this.guideCompletedCount = guideCompletedCount;
    }

    public long getGuideInProgressCount() {
        return guideInProgressCount;
    }

    public void setGuideInProgressCount(long guideInProgressCount) {
        this.guideInProgressCount = guideInProgressCount;
    }

    public long getGuideNotStartedCount() {
        return guideNotStartedCount;
    }

    public void setGuideNotStartedCount(long guideNotStartedCount) {
        this.guideNotStartedCount = guideNotStartedCount;
    }

    public List<FormationStatsTopItemDto> getTopViewedFormations() {
        return topViewedFormations;
    }

    public void setTopViewedFormations(List<FormationStatsTopItemDto> topViewedFormations) {
        this.topViewedFormations = topViewedFormations;
    }

    public List<FormationStatsTopItemDto> getTopLikedFormations() {
        return topLikedFormations;
    }

    public void setTopLikedFormations(List<FormationStatsTopItemDto> topLikedFormations) {
        this.topLikedFormations = topLikedFormations;
    }

    public GuideProgressDistributionDto getGuideProgressDistribution() {
        return guideProgressDistribution;
    }

    public void setGuideProgressDistribution(GuideProgressDistributionDto guideProgressDistribution) {
        this.guideProgressDistribution = guideProgressDistribution;
    }

    public List<FormationViewsEvolutionPointDto> getViewsEvolution() {
        return viewsEvolution;
    }

    public void setViewsEvolution(List<FormationViewsEvolutionPointDto> viewsEvolution) {
        this.viewsEvolution = viewsEvolution;
    }
}
