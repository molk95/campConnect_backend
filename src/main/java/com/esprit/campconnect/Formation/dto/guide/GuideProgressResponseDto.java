package com.esprit.campconnect.Formation.dto.guide;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GuideProgressResponseDto {

    private Long guideId;
    private Long utilisateurId;
    private Integer totalSteps;
    private Integer completedSteps;
    private Double progressPercent;
    private Boolean completed;
    private Boolean rewardUnlocked;
    private String rewardMessage;
    private String badge;
    private Integer pointsAwarded;
    private String bonusTemplate;
    private Long nextStepId;
    private List<Long> completedStepIds = new ArrayList<>();
    private Integer minimumQuizScore;
    private Integer quizScore;
    private Boolean quizPassed;
    private LocalDateTime quizAttemptedAt;
    private LocalDateTime rewardUnlockedAt;
    private LocalDateTime lastUpdated;

    public GuideProgressResponseDto() {
    }

    public Long getGuideId() {
        return guideId;
    }

    public void setGuideId(Long guideId) {
        this.guideId = guideId;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public Integer getCompletedSteps() {
        return completedSteps;
    }

    public void setCompletedSteps(Integer completedSteps) {
        this.completedSteps = completedSteps;
    }

    public Double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Double progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Boolean getRewardUnlocked() {
        return rewardUnlocked;
    }

    public void setRewardUnlocked(Boolean rewardUnlocked) {
        this.rewardUnlocked = rewardUnlocked;
    }

    public String getRewardMessage() {
        return rewardMessage;
    }

    public void setRewardMessage(String rewardMessage) {
        this.rewardMessage = rewardMessage;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public Integer getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(Integer pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public String getBonusTemplate() {
        return bonusTemplate;
    }

    public void setBonusTemplate(String bonusTemplate) {
        this.bonusTemplate = bonusTemplate;
    }

    public Long getNextStepId() {
        return nextStepId;
    }

    public void setNextStepId(Long nextStepId) {
        this.nextStepId = nextStepId;
    }

    public List<Long> getCompletedStepIds() {
        return completedStepIds;
    }

    public void setCompletedStepIds(List<Long> completedStepIds) {
        this.completedStepIds = completedStepIds;
    }

    public Integer getMinimumQuizScore() {
        return minimumQuizScore;
    }

    public void setMinimumQuizScore(Integer minimumQuizScore) {
        this.minimumQuizScore = minimumQuizScore;
    }

    public Integer getQuizScore() {
        return quizScore;
    }

    public void setQuizScore(Integer quizScore) {
        this.quizScore = quizScore;
    }

    public Boolean getQuizPassed() {
        return quizPassed;
    }

    public void setQuizPassed(Boolean quizPassed) {
        this.quizPassed = quizPassed;
    }

    public LocalDateTime getQuizAttemptedAt() {
        return quizAttemptedAt;
    }

    public void setQuizAttemptedAt(LocalDateTime quizAttemptedAt) {
        this.quizAttemptedAt = quizAttemptedAt;
    }

    public LocalDateTime getRewardUnlockedAt() {
        return rewardUnlockedAt;
    }

    public void setRewardUnlockedAt(LocalDateTime rewardUnlockedAt) {
        this.rewardUnlockedAt = rewardUnlockedAt;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
