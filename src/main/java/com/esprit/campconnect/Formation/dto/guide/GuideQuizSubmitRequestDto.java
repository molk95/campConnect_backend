package com.esprit.campconnect.Formation.dto.guide;

import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GuideQuizSubmitRequestDto {

    @NotNull(message = "Les reponses sont obligatoires")
    private Map<Long, String> answersByQuestionId = new HashMap<>();

    public Map<Long, String> getAnswersByQuestionId() {
        return answersByQuestionId;
    }

    public void setAnswersByQuestionId(Map<Long, String> answersByQuestionId) {
        this.answersByQuestionId = answersByQuestionId;
    }
}
