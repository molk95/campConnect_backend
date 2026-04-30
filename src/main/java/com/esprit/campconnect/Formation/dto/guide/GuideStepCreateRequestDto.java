package com.esprit.campconnect.Formation.dto.guide;

import com.esprit.campconnect.Formation.entity.guide.GuideStepMediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class GuideStepCreateRequestDto {

    @Positive(message = "L'ordre de l'etape doit etre > 0")
    private Integer stepOrder;

    @NotBlank(message = "Le titre de l'etape est obligatoire")
    @Size(max = 200, message = "Le titre de l'etape ne doit pas depasser 200 caracteres")
    private String titre;

    @NotBlank(message = "La description de l'etape est obligatoire")
    @Size(max = 5000, message = "La description de l'etape ne doit pas depasser 5000 caracteres")
    private String description;

    private GuideStepMediaType mediaType;

    @Size(max = 1200, message = "L'URL media ne doit pas depasser 1200 caracteres")
    private String mediaUrl;

    @Size(max = 2500, message = "La checklist ne doit pas depasser 2500 caracteres")
    private String checklist;

    public GuideStepCreateRequestDto() {
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
