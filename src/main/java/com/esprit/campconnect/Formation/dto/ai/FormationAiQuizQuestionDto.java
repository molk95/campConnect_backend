package com.esprit.campconnect.Formation.dto.ai;

import java.util.List;

public class FormationAiQuizQuestionDto {

    private String question;
    private List<String> choices;
    private String answer;
    private String explanation;

    public FormationAiQuizQuestionDto() {
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
