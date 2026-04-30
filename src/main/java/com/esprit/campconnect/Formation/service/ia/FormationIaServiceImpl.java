package com.esprit.campconnect.Formation.service.ia;

import com.esprit.campconnect.Formation.dto.ai.FormationAiGenerateRequestDto;
import com.esprit.campconnect.Formation.dto.ai.FormationAiGenerateResponseDto;
import com.esprit.campconnect.Formation.dto.ai.FormationAiQuizQuestionDto;
import com.esprit.campconnect.Formation.entity.FormationLevel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormationIaServiceImpl implements FormationIaService {

    @Override
    public FormationAiGenerateResponseDto generateContent(FormationAiGenerateRequestDto request) {
        String sujet = request.getSujet().trim();
        FormationLevel level = request.getLevel() != null ? request.getLevel() : FormationLevel.BEGINNER;
        int duration = request.getDuration() != null ? request.getDuration() : 60;

        FormationAiGenerateResponseDto response = new FormationAiGenerateResponseDto();
        response.setCours(buildCourse(sujet, level, duration));
        response.setResume(buildSummary(sujet, level));
        response.setExemples(buildExamples(sujet));
        response.setQuiz(buildQuiz(sujet, level));
        return response;
    }

    private String buildCourse(String sujet, FormationLevel level, int duration) {
        return "Cours genere pour le sujet '" + sujet + "' au niveau " + level
                + " avec une duree cible de " + duration + " minutes. "
                + "Objectifs: comprendre les concepts, appliquer sur un cas reel, et valider les acquis.";
    }

    private String buildSummary(String sujet, FormationLevel level) {
        return "Resume " + level + ": les points essentiels de '" + sujet
                + "' avec les bonnes pratiques a retenir avant la mise en pratique.";
    }

    private List<String> buildExamples(String sujet) {
        return List.of(
                "Exemple 1: scenario d'introduction autour de '" + sujet + "'.",
                "Exemple 2: cas pratique intermediaire avec erreurs frequentes.",
                "Exemple 3: mini-projet final pour consolider les acquis."
        );
    }

    private List<FormationAiQuizQuestionDto> buildQuiz(String sujet, FormationLevel level) {
        FormationAiQuizQuestionDto q1 = new FormationAiQuizQuestionDto();
        q1.setQuestion("Quel est le premier objectif de la formation sur '" + sujet + "' ?");
        q1.setChoices(List.of("Comprendre les bases", "Supprimer la theorie", "Ignorer la pratique"));
        q1.setAnswer("Comprendre les bases");
        q1.setExplanation("La progression commence toujours par les fondamentaux.");

        FormationAiQuizQuestionDto q2 = new FormationAiQuizQuestionDto();
        q2.setQuestion("Au niveau " + level + ", quelle approche est recommandee ?");
        q2.setChoices(List.of("Pratique guidee", "Memorisation sans exercices", "Evaluation sans cours"));
        q2.setAnswer("Pratique guidee");
        q2.setExplanation("L'apprentissage est plus solide avec une pratique guidee.");

        return List.of(q1, q2);
    }
}
