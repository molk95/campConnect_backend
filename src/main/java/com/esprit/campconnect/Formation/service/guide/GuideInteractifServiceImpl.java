package com.esprit.campconnect.Formation.service.guide;

import com.esprit.campconnect.Formation.dto.guide.GuideCreateRequestDto;
import com.esprit.campconnect.Formation.dto.guide.GuideProgressResponseDto;
import com.esprit.campconnect.Formation.dto.guide.GuideResponseDto;
import com.esprit.campconnect.Formation.dto.guide.GuideStepCreateRequestDto;
import com.esprit.campconnect.Formation.dto.guide.GuideStepResponseDto;
import com.esprit.campconnect.Formation.entity.Formation;
import com.esprit.campconnect.Formation.entity.guide.GuideInteractif;
import com.esprit.campconnect.Formation.entity.guide.GuideProgress;
import com.esprit.campconnect.Formation.entity.guide.GuideStep;
import com.esprit.campconnect.Formation.entity.guide.GuideStepCompletion;
import com.esprit.campconnect.Formation.entity.guide.GuideStepMediaType;
import com.esprit.campconnect.Formation.entity.guide.UserReward;
import com.esprit.campconnect.Formation.repository.FormationRepository;
import com.esprit.campconnect.Formation.repository.guide.GuideInteractifRepository;
import com.esprit.campconnect.Formation.repository.guide.GuideProgressRepository;
import com.esprit.campconnect.Formation.repository.guide.GuideStepCompletionRepository;
import com.esprit.campconnect.Formation.repository.guide.GuideStepRepository;
import com.esprit.campconnect.Formation.repository.guide.UserRewardRepository;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GuideInteractifServiceImpl implements GuideInteractifService {

    private static final String FINAL_BADGE = "Explorateur du camping";
    private static final int FINAL_POINTS = 50;
    private static final String FINAL_BONUS = "Template bonus: checklist complete d'organisation d'un camping reussi.";

    private final FormationRepository formationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final GuideInteractifRepository guideInteractifRepository;
    private final GuideStepRepository guideStepRepository;
    private final GuideProgressRepository guideProgressRepository;
    private final GuideStepCompletionRepository guideStepCompletionRepository;
    private final UserRewardRepository userRewardRepository;

    @Override
    @Transactional
    public GuideResponseDto createGuide(Long formationId, GuideCreateRequestDto request) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation introuvable"));

        if (guideInteractifRepository.findByFormation_Id(formationId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette formation a deja un guide interactif");
        }

        GuideInteractif guide = new GuideInteractif();
        guide.setFormation(formation);
        guide.setTitre(request.getTitre().trim());
        guide.setDescription(request.getDescription().trim());
        guide.setRecompenseFinale(request.getRecompenseFinale().trim());

        GuideInteractif saved = guideInteractifRepository.save(guide);
        return toGuideResponse(saved, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public GuideResponseDto getGuideByFormation(Long formationId) {
        GuideInteractif guide = guideInteractifRepository.findByFormation_Id(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guide interactif introuvable pour cette formation"));
        int stepsCount = guideStepRepository.findByGuide_IdOrderByStepOrderAsc(guide.getId()).size();
        return toGuideResponse(guide, stepsCount);
    }

    @Override
    @Transactional
    public GuideStepResponseDto addStep(Long guideId, GuideStepCreateRequestDto request) {
        GuideInteractif guide = guideInteractifRepository.findById(guideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guide interactif introuvable"));

        if (guideStepRepository.existsByGuide_IdAndStepOrder(guideId, request.getStepOrder())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet ordre d'etape existe deja pour ce guide");
        }

        GuideStep step = new GuideStep();
        step.setGuide(guide);
        step.setStepOrder(request.getStepOrder());
        step.setTitre(request.getTitre().trim());
        step.setDescription(request.getDescription().trim());
        step.setMediaType(request.getMediaType() != null ? request.getMediaType() : GuideStepMediaType.NONE);
        step.setMediaUrl(normalizeBlank(request.getMediaUrl()));
        step.setChecklist(normalizeBlank(request.getChecklist()));

        GuideStep saved = guideStepRepository.save(step);
        return toStepResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuideStepResponseDto> getSteps(Long guideId) {
        if (!guideInteractifRepository.existsById(guideId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Guide interactif introuvable");
        }

        return guideStepRepository.findByGuide_IdOrderByStepOrderAsc(guideId).stream()
                .map(this::toStepResponse)
                .toList();
    }

    @Override
    @Transactional
    public GuideProgressResponseDto completeStep(Long guideId, Long stepId, Long utilisateurId) {
        GuideInteractif guide = guideInteractifRepository.findById(guideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guide interactif introuvable"));

        GuideStep step = guideStepRepository.findByIdAndGuide_Id(stepId, guideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Etape introuvable pour ce guide"));

        Utilisateur user = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        List<GuideStep> steps = guideStepRepository.findByGuide_IdOrderByStepOrderAsc(guideId);
        if (steps.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucune etape disponible pour ce guide");
        }

        GuideProgress progress = getOrCreateProgress(guide, user, steps.size());
        if (guideStepCompletionRepository.existsByProgress_IdAndStep_Id(progress.getId(), stepId)) {
            return buildProgressResponse(progress, guide, steps);
        }

        assertSequentialProgress(progress.getId(), steps, step.getStepOrder());

        GuideStepCompletion completion = new GuideStepCompletion();
        completion.setProgress(progress);
        completion.setStep(step);
        guideStepCompletionRepository.save(completion);

        long completedCount = guideStepCompletionRepository.countByProgress_Id(progress.getId());
        updateProgress(progress, (int) completedCount, steps.size());
        guideProgressRepository.save(progress);
        ensureRewardAtCompletion(progress, guide);

        return buildProgressResponse(progress, guide, steps);
    }

    @Override
    @Transactional
    public GuideProgressResponseDto getProgress(Long guideId, Long utilisateurId) {
        GuideInteractif guide = guideInteractifRepository.findById(guideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guide interactif introuvable"));

        if (!utilisateurRepository.existsById(utilisateurId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
        }

        List<GuideStep> steps = guideStepRepository.findByGuide_IdOrderByStepOrderAsc(guideId);
        GuideProgress progress = guideProgressRepository.findByGuide_IdAndUtilisateur_Id(guideId, utilisateurId)
                .orElse(null);

        if (progress == null) {
            GuideProgressResponseDto dto = new GuideProgressResponseDto();
            dto.setGuideId(guideId);
            dto.setUtilisateurId(utilisateurId);
            dto.setTotalSteps(steps.size());
            dto.setCompletedSteps(0);
            dto.setProgressPercent(0D);
            dto.setCompleted(false);
            dto.setRewardUnlocked(false);
            dto.setRewardMessage(null);
            dto.setBadge(null);
            dto.setPointsAwarded(0);
            dto.setBonusTemplate(null);
            dto.setNextStepId(steps.isEmpty() ? null : steps.get(0).getId());
            dto.setRewardUnlockedAt(null);
            dto.setLastUpdated(null);
            return dto;
        }

        long completedCount = guideStepCompletionRepository.countByProgress_Id(progress.getId());
        updateProgress(progress, (int) completedCount, steps.size());
        guideProgressRepository.save(progress);
        ensureRewardAtCompletion(progress, guide);

        return buildProgressResponse(progress, guide, steps);
    }

    private GuideProgress getOrCreateProgress(GuideInteractif guide, Utilisateur user, int totalSteps) {
        return guideProgressRepository.findByGuide_IdAndUtilisateur_Id(guide.getId(), user.getId())
                .orElseGet(() -> {
                    GuideProgress newProgress = new GuideProgress();
                    newProgress.setGuide(guide);
                    newProgress.setUtilisateur(user);
                    newProgress.setTotalSteps(totalSteps);
                    newProgress.setCompletedSteps(0);
                    newProgress.setProgressPercent(0D);
                    newProgress.setCompleted(false);
                    newProgress.setRewardUnlocked(false);
                    newProgress.setLastUpdated(LocalDateTime.now());
                    return guideProgressRepository.save(newProgress);
                });
    }

    private void assertSequentialProgress(Long progressId, List<GuideStep> steps, int targetStepOrder) {
        List<GuideStep> requiredPreviousSteps = steps.stream()
                .filter(s -> s.getStepOrder() < targetStepOrder)
                .toList();

        for (GuideStep previous : requiredPreviousSteps) {
            boolean done = guideStepCompletionRepository.existsByProgress_IdAndStep_Id(progressId, previous.getId());
            if (!done) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous devez terminer les etapes precedentes d'abord");
            }
        }
    }

    private void updateProgress(GuideProgress progress, int completedCount, int totalSteps) {
        double percent = totalSteps == 0 ? 0D : (completedCount * 100.0) / totalSteps;
        boolean done = totalSteps > 0 && completedCount >= totalSteps;

        progress.setTotalSteps(totalSteps);
        progress.setCompletedSteps(completedCount);
        progress.setProgressPercent(percent);
        progress.setCompleted(done);
        progress.setLastUpdated(LocalDateTime.now());

        if (done && !Boolean.TRUE.equals(progress.getRewardUnlocked())) {
            progress.setRewardUnlocked(true);
            progress.setRewardUnlockedAt(LocalDateTime.now());
        }
    }

    private GuideProgressResponseDto buildProgressResponse(GuideProgress progress, GuideInteractif guide, List<GuideStep> steps) {
        GuideProgressResponseDto dto = new GuideProgressResponseDto();
        dto.setGuideId(guide.getId());
        dto.setUtilisateurId(progress.getUtilisateur().getId());
        dto.setTotalSteps(progress.getTotalSteps());
        dto.setCompletedSteps(progress.getCompletedSteps());
        dto.setProgressPercent(progress.getProgressPercent());
        dto.setCompleted(progress.getCompleted());
        dto.setRewardUnlocked(progress.getRewardUnlocked());
        dto.setRewardUnlockedAt(progress.getRewardUnlockedAt());
        dto.setLastUpdated(progress.getLastUpdated());

        if (Boolean.TRUE.equals(progress.getRewardUnlocked())) {
            dto.setRewardMessage(guide.getRecompenseFinale());
        }
        enrichWithReward(dto, guide.getId(), progress.getUtilisateur().getId());

        dto.setNextStepId(resolveNextStepId(progress.getId(), steps));
        return dto;
    }

    private Long resolveNextStepId(Long progressId, List<GuideStep> steps) {
        Optional<GuideStep> next = steps.stream()
                .filter(step -> !guideStepCompletionRepository.existsByProgress_IdAndStep_Id(progressId, step.getId()))
                .findFirst();
        return next.map(GuideStep::getId).orElse(null);
    }

    private GuideResponseDto toGuideResponse(GuideInteractif guide, int stepsCount) {
        GuideResponseDto dto = new GuideResponseDto();
        dto.setId(guide.getId());
        dto.setFormationId(guide.getFormation().getId());
        dto.setTitre(guide.getTitre());
        dto.setDescription(guide.getDescription());
        dto.setRecompenseFinale(guide.getRecompenseFinale());
        dto.setStepsCount(stepsCount);
        dto.setCreatedAt(guide.getCreatedAt());
        return dto;
    }

    private GuideStepResponseDto toStepResponse(GuideStep step) {
        GuideStepResponseDto dto = new GuideStepResponseDto();
        dto.setId(step.getId());
        dto.setStepOrder(step.getStepOrder());
        dto.setTitre(step.getTitre());
        dto.setDescription(step.getDescription());
        dto.setMediaType(step.getMediaType() != null ? step.getMediaType() : GuideStepMediaType.NONE);
        dto.setMediaUrl(step.getMediaUrl());
        dto.setChecklist(step.getChecklist());
        return dto;
    }

    private void ensureRewardAtCompletion(GuideProgress progress, GuideInteractif guide) {
        if (!Boolean.TRUE.equals(progress.getCompleted()) || !Boolean.TRUE.equals(progress.getRewardUnlocked())) {
            return;
        }

        if (userRewardRepository.findByGuide_IdAndUtilisateur_Id(guide.getId(), progress.getUtilisateur().getId()).isPresent()) {
            return;
        }

        UserReward reward = new UserReward();
        reward.setGuide(guide);
        reward.setUtilisateur(progress.getUtilisateur());
        reward.setBadge(FINAL_BADGE);
        reward.setPoints(FINAL_POINTS);
        reward.setBonus(FINAL_BONUS);
        userRewardRepository.save(reward);
    }

    private void enrichWithReward(GuideProgressResponseDto dto, Long guideId, Long utilisateurId) {
        Optional<UserReward> rewardOpt = userRewardRepository.findByGuide_IdAndUtilisateur_Id(guideId, utilisateurId);
        if (rewardOpt.isPresent()) {
            UserReward reward = rewardOpt.get();
            dto.setBadge(reward.getBadge());
            dto.setPointsAwarded(reward.getPoints());
            dto.setBonusTemplate(reward.getBonus());
            return;
        }

        dto.setBadge(null);
        dto.setPointsAwarded(0);
        dto.setBonusTemplate(null);
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
