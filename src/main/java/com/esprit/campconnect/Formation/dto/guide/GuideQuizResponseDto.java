package com.esprit.campconnect.Formation.dto.guide;

import java.util.ArrayList;
import java.util.List;

public class GuideQuizResponseDto {

    private Long formationId;
    private String quizTitle;
    private Integer minimumScore;
    private Integer questionCount;
    private List<GuideQuizQuestionResponseDto> questions = new ArrayList<>();

    public Long getFormationId() {
        return formationId;
    }

    public void setFormationId(Long formationId) {
        this.formationId = formationId;
    }

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

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public List<GuideQuizQuestionResponseDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<GuideQuizQuestionResponseDto> questions) {
        this.questions = questions;
    }
}
