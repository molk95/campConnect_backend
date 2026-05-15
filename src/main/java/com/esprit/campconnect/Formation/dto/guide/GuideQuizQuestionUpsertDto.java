package com.esprit.campconnect.Formation.dto.guide;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class GuideQuizQuestionUpsertDto {

    @Positive(message = "L'ordre de la question doit etre > 0")
    private Integer questionOrder;

    @NotBlank(message = "Le libelle de la question est obligatoire")
    @Size(max = 1200, message = "La question ne doit pas depasser 1200 caracteres")
    private String question;

    @NotBlank(message = "Option A obligatoire")
    @Size(max = 500, message = "Option A trop longue")
    private String optionA;

    @NotBlank(message = "Option B obligatoire")
    @Size(max = 500, message = "Option B trop longue")
    private String optionB;

    @NotBlank(message = "Option C obligatoire")
    @Size(max = 500, message = "Option C trop longue")
    private String optionC;

    @NotBlank(message = "Option D obligatoire")
    @Size(max = 500, message = "Option D trop longue")
    private String optionD;

    @NotBlank(message = "La bonne reponse est obligatoire")
    @Pattern(regexp = "^[ABCDabcd]$", message = "La bonne reponse doit etre A, B, C ou D")
    private String correctOption;

    @Size(max = 2000, message = "L'explication ne doit pas depasser 2000 caracteres")
    private String explanation;

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
