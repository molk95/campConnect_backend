package com.esprit.campconnect.Formation.service;

import com.esprit.campconnect.Formation.entity.Formation;
import com.esprit.campconnect.Formation.entity.FormationLike;
import com.esprit.campconnect.Formation.entity.FormationLevel;
import com.esprit.campconnect.Formation.entity.FormationStatus;
import com.esprit.campconnect.Formation.repository.FormationLikeRepository;
import com.esprit.campconnect.Formation.repository.FormationRepository;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormationServiceImpl implements FormationService {

    private final FormationRepository formationRepository;
    private final FormationLikeRepository formationLikeRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<Formation> getAll() {
        return formationRepository.findAllByOrderByDateCreationDesc();
    }

    @Override
    public Formation getById(Long id) {
        return formationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation introuvable"));
    }

    @Override
    public List<Formation> getByGuide(Long guideId) {
        return formationRepository.findByGuide_IdOrderByDateCreationDesc(guideId);
    }

    @Override
    public Page<Formation> search(String query,
                                  Long guideId,
                                  FormationStatus status,
                                  LocalDateTime dateFrom,
                                  LocalDateTime dateTo,
                                  Pageable pageable) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        return formationRepository.search(normalizedQuery, guideId, status, dateFrom, dateTo, pageable);
    }

    @Override
    public Formation create(Formation formation) {
        if (formation.getLevel() == null) {
            formation.setLevel(FormationLevel.BEGINNER);
        }
        if (formation.getDuration() == null || formation.getDuration() <= 0) {
            formation.setDuration(60);
        }
        if (formation.getStatus() == null) {
            formation.setStatus(FormationStatus.DRAFT);
        }
        return formationRepository.save(formation);
    }

    @Override
    public Formation update(Long id, Formation formation) {
        Formation existing = getById(id);
        existing.setTitre(formation.getTitre());
        existing.setDescription(formation.getDescription());
        if (formation.getLevel() != null) {
            existing.setLevel(formation.getLevel());
        }
        if (formation.getDuration() != null && formation.getDuration() > 0) {
            existing.setDuration(formation.getDuration());
        }
        if (formation.getStatus() != null) {
            existing.setStatus(formation.getStatus());
        }
        return formationRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Formation existing = getById(id);
        formationRepository.delete(existing);
    }

    @Override
    @Transactional
    public void like(Long formationId, Long utilisateurId) {
        Formation formation = getById(formationId);
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (formationLikeRepository.existsByFormation_IdAndUtilisateur_Id(formationId, utilisateurId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Formation deja likee par cet utilisateur");
        }

        FormationLike like = new FormationLike();
        like.setFormation(formation);
        like.setUtilisateur(utilisateur);
        formationLikeRepository.save(like);
    }

    @Override
    public long getLikeCount(Long formationId) {
        return formationLikeRepository.countByFormation_Id(formationId);
    }

    @Override
    public boolean isLikedByUser(Long formationId, Long utilisateurId) {
        if (utilisateurId == null) {
            return false;
        }
        return formationLikeRepository.existsByFormation_IdAndUtilisateur_Id(formationId, utilisateurId);
    }

    @Override
    public Map<Long, Long> getLikeCountByFormationIds(List<Long> formationIds) {
        if (formationIds == null || formationIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> counts = new HashMap<>();
        formationLikeRepository.countLikesByFormationIds(formationIds)
                .forEach(row -> counts.put(row.getFormationId(), row.getLikeCount()));
        return counts;
    }

    @Override
    public Set<Long> getLikedFormationIdsByUser(List<Long> formationIds, Long utilisateurId) {
        if (utilisateurId == null || formationIds == null || formationIds.isEmpty()) {
            return Set.of();
        }

        return formationLikeRepository.findLikedFormationIdsByUserAndFormationIds(utilisateurId, formationIds)
                .stream()
                .collect(Collectors.toSet());
    }
}
