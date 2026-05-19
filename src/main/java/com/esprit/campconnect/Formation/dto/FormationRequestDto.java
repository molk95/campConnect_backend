package com.esprit.campconnect.Formation.dto;

import com.esprit.campconnect.Formation.entity.FormationStatus;
import com.esprit.campconnect.Formation.entity.FormationLevel;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public class FormationRequestDto {

    @JsonAlias({"title", "nom"})
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150, message = "Le titre ne doit pas depasser 150 caracteres")
    private String titre;

    @JsonAlias({"content", "contenu", "desc"})
    @NotBlank(message = "La description est obligatoire")
    @Size(max = 4000, message = "La description ne doit pas depasser 4000 caracteres")
    private String description;

    @JsonAlias({"niveau"})
    private FormationLevel level;

    @JsonAlias({"duree", "durationMinutes", "dureeMinutes"})
    @Positive(message = "La duree doit etre > 0")
    private Integer duration;

    @JsonAlias({"statut"})
    private FormationStatus status;

    @JsonAlias({"objectives"})
    private List<String> objectifs;

    @JsonAlias({"objectifsText", "objectivesText"})
    @Size(max = 8000, message = "Le texte des objectifs ne doit pas depasser 8000 caracteres")
    private String objectifsText;

    @JsonAlias({"quizTitre"})
    @Size(max = 200, message = "Le titre du quiz ne doit pas depasser 200 caracteres")
    private String quizTitle;

    @JsonAlias({"minimumScore"})
    @Min(value = 0, message = "Le score minimum doit etre >= 0")
    @Max(value = 100, message = "Le score minimum doit etre <= 100")
    private Integer quizMinimumScore;

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

    public List<String> getObjectifs() {
        return objectifs;
    }

    public void setObjectifs(List<String> objectifs) {
        this.objectifs = objectifs;
    }

    public String getObjectifsText() {
        return objectifsText;
    }

    public void setObjectifsText(String objectifsText) {
        this.objectifsText = objectifsText;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public Integer getQuizMinimumScore() {
        return quizMinimumScore;
    }

    public void setQuizMinimumScore(Integer quizMinimumScore) {
        this.quizMinimumScore = quizMinimumScore;
    }
}
