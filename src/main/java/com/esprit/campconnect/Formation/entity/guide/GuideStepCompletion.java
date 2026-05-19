package com.esprit.campconnect.Formation.entity.guide;

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
@Table(name = "guide_step_completion", uniqueConstraints = {
        @UniqueConstraint(name = "uk_guide_step_completion_progress_step", columnNames = {"progress_id", "step_id"})
})
public class GuideStepCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progress_id", nullable = false)
    @JsonIgnore
    private GuideProgress progress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    @JsonIgnore
    private GuideStep step;

    @Column(nullable = false, updatable = false)
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GuideProgress getProgress() {
        return progress;
    }

    public void setProgress(GuideProgress progress) {
        this.progress = progress;
    }

    public GuideStep getStep() {
        return step;
    }

    public void setStep(GuideStep step) {
        this.step = step;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
