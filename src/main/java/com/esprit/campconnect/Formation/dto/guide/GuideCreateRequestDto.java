package com.esprit.campconnect.Formation.dto.guide;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GuideCreateRequestDto {

    @NotBlank(message = "Le titre du guide est obligatoire")
    @Size(max = 180, message = "Le titre du guide ne doit pas depasser 180 caracteres")
    private String titre;

    @NotBlank(message = "La description du guide est obligatoire")
    @Size(max = 3000, message = "La description du guide ne doit pas depasser 3000 caracteres")
    private String description;

    @NotBlank(message = "La recompense finale est obligatoire")
    @Size(max = 300, message = "La recompense finale ne doit pas depasser 300 caracteres")
    private String recompenseFinale;

    public GuideCreateRequestDto() {
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
}
