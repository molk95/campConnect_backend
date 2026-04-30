package com.esprit.campconnect.Formation.service;

import com.esprit.campconnect.Formation.entity.Formation;
import com.esprit.campconnect.Formation.entity.FormationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.Set;

public interface FormationService {

    List<Formation> getAll();

    Formation getById(Long id);

    List<Formation> getByGuide(Long guideId);

    Page<Formation> search(
            String query,
            Long guideId,
            FormationStatus status,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    );

    Formation create(Formation formation);

    Formation update(Long id, Formation formation);

    void delete(Long id);

    void like(Long formationId, Long utilisateurId);

    long getLikeCount(Long formationId);

    boolean isLikedByUser(Long formationId, Long utilisateurId);

    Map<Long, Long> getLikeCountByFormationIds(List<Long> formationIds);

    Set<Long> getLikedFormationIdsByUser(List<Long> formationIds, Long utilisateurId);
}
