package com.esprit.campconnect.Formation.repository.guide;

import com.esprit.campconnect.Formation.entity.guide.UserReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRewardRepository extends JpaRepository<UserReward, Long> {
    Optional<UserReward> findByGuide_IdAndUtilisateur_Id(Long guideId, Long utilisateurId);
}
