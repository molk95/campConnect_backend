package com.esprit.campconnect.Formation.dto.guide;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class GuideQuizUpsertRequestDto {

    @Size(max = 200, message = "Le titre du quiz ne doit pas depasser 200 caracteres")
    private String quizTitle;

    @Min(value = 0, message = "Le score minimum doit etre >= 0")
    @Max(value = 100, message = "Le score minimum doit etre <= 100")
    private Integer minimumScore;

    @Valid
    private List<GuideQuizQuestionUpsertDto> questions = new ArrayList<>();

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public Integer getMinimumScore() {
        return minimumScore;
    }

    public void setMinimumScore(Integer minimumScore) {
        this.minimumScore = minimumScore;
    }

    public List<GuideQuizQuestionUpsertDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<GuideQuizQuestionUpsertDto> questions) {
        this.questions = questions;
    }
}
