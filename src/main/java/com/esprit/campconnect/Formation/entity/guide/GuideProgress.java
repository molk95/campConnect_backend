package com.esprit.campconnect.Formation.entity.guide;

import com.esprit.campconnect.User.Entity.Utilisateur;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "guide_progress_user", uniqueConstraints = {
        @UniqueConstraint(name = "uk_guide_progress_guide_user", columnNames = {"guide_id", "utilisateur_id"})
})
public class GuideProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    @JsonIgnore
    private GuideInteractif guide;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    @JsonIgnore
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private Integer totalSteps;

    @Column(nullable = false)
    private Integer completedSteps;

    @Column(nullable = false)
    private Double progressPercent;

    @Column(nullable = false)
    private Boolean completed;

    @Column(nullable = false)
    private Boolean rewardUnlocked;

    private LocalDateTime rewardUnlockedAt;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        if (this.totalSteps == null) {
            this.totalSteps = 0;
        }
        if (this.completedSteps == null) {
            this.completedSteps = 0;
        }
        if (this.progressPercent == null) {
            this.progressPercent = 0D;
        }
        if (this.completed == null) {
            this.completed = false;
        }
        if (this.rewardUnlocked == null) {
            this.rewardUnlocked = false;
        }
        if (this.lastUpdated == null) {
            this.lastUpdated = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GuideInteractif getGuide() {
        return guide;
    }

    public void setGuide(GuideInteractif guide) {
        this.guide = guide;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
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
