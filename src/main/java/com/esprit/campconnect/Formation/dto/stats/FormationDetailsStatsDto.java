package com.esprit.campconnect.Formation.dto.stats;

import java.util.ArrayList;
import java.util.List;

public class FormationDetailsStatsDto {

    private Long formationId;
    private String title;
    private long viewsCount;
    private long likesCount;
    private double completionRate;
    private double averageProgress;
    private double averageQuizScore;
    private List<FormationViewsEvolutionPointDto> viewsEvolution = new ArrayList<>();

    public Long getFormationId() {
        return formationId;
    }

    public void setFormationId(Long formationId) {
        this.formationId = formationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public double getAverageProgress() {
        return averageProgress;
    }

    public void setAverageProgress(double averageProgress) {
        this.averageProgress = averageProgress;
    }

    public double getAverageQuizScore() {
        return averageQuizScore;
    }

    public void setAverageQuizScore(double averageQuizScore) {
        this.averageQuizScore = averageQuizScore;
    }

    public List<FormationViewsEvolutionPointDto> getViewsEvolution() {
        return viewsEvolution;
    }

    public void setViewsEvolution(List<FormationViewsEvolutionPointDto> viewsEvolution) {
        this.viewsEvolution = viewsEvolution;
    }
}
