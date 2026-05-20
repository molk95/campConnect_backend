package com.esprit.campconnect.Formation.dto.guide;

import java.time.LocalDateTime;

public class GuideResponseDto {

    private Long id;
    private Long formationId;
    private String titre;
    private String description;
    private String recompenseFinale;
    private Integer stepsCount;
    private LocalDateTime createdAt;

    public GuideResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFormationId() {
        return formationId;
    }

    public void setFormationId(Long formationId) {
        this.formationId = formationId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecompenseFinale() {
        return recompenseFinale;
    }

    public void setRecompenseFinale(String recompenseFinale) {
        this.recompenseFinale = recompenseFinale;
    }

    public Integer getStepsCount() {
        return stepsCount;
    }

    public void setStepsCount(Integer stepsCount) {
        this.stepsCount = stepsCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
