package com.esprit.campconnect.Formation.dto;

import com.esprit.campconnect.Formation.entity.FormationStatus;
import com.esprit.campconnect.Formation.entity.FormationLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class FormationRequestDto {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150, message = "Le titre ne doit pas depasser 150 caracteres")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 4000, message = "La description ne doit pas depasser 4000 caracteres")
    private String description;

    private FormationLevel level;

    @Positive(message = "La duree doit etre > 0")
    private Integer duration;

    private FormationStatus status;

    public FormationRequestDto() {
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

    public FormationStatus getStatus() {
        return status;
    }

    public void setStatus(FormationStatus status) {
        this.status = status;
    }

    public FormationLevel getLevel() {
        return level;
    }

    public void setLevel(FormationLevel level) {
        this.level = level;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
