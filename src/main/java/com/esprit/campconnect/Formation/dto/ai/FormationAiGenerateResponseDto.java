package com.esprit.campconnect.Formation.dto.ai;

import java.util.ArrayList;
import java.util.List;

public class FormationAiGenerateResponseDto {

    private String cours;
    private String resume;
    private List<String> exemples = new ArrayList<>();
    private List<FormationAiQuizQuestionDto> quiz = new ArrayList<>();

    public FormationAiGenerateResponseDto() {
    }

    public String getCours() {
        return cours;
    }

    public void setCours(String cours) {
        this.cours = cours;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public List<String> getExemples() {
        return exemples;
    }

    public void setExemples(List<String> exemples) {
        this.exemples = exemples;
    }

    public List<FormationAiQuizQuestionDto> getQuiz() {
        return quiz;
    }

    public void setQuiz(List<FormationAiQuizQuestionDto> quiz) {
        this.quiz = quiz;
    }
}
