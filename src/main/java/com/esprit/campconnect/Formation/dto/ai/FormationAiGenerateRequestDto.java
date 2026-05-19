package com.esprit.campconnect.Formation.dto.ai;

import com.esprit.campconnect.Formation.entity.FormationLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class FormationAiGenerateRequestDto {

    @NotBlank(message = "Le sujet est obligatoire")
    private String sujet;

    private FormationLevel level;

    @Positive(message = "La duree cible doit etre > 0")
    private Integer duration;

    public FormationAiGenerateRequestDto() {
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
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
