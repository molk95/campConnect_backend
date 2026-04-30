package com.esprit.campconnect.Formation.dto.guide;

import com.esprit.campconnect.Formation.entity.guide.GuideStepMediaType;

public class GuideStepResponseDto {

    private Long id;
    private Integer stepOrder;
    private String titre;
    private String description;
    private GuideStepMediaType mediaType;
    private String mediaUrl;
    private String checklist;

    public GuideStepResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
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

    public GuideStepMediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(GuideStepMediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getChecklist() {
        return checklist;
    }

    public void setChecklist(String checklist) {
        this.checklist = checklist;
    }
}
