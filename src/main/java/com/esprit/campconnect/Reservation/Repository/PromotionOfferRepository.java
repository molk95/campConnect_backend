package com.esprit.campconnect.Reservation.Repository;

import com.esprit.campconnect.Reservation.Entity.PromotionOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionOfferRepository extends JpaRepository<PromotionOffer, Long> {

    Optional<PromotionOffer> findByCodeIgnoreCase(String code);

    List<PromotionOffer> findByAutoApplyTrueAndActiveTrueOrderByDateCreationDesc();

    List<PromotionOffer> findByDiscoverableTrueAndActiveTrueOrderByAutoApplyDescDateCreationDesc();

    boolean existsByCodeIgnoreCase(String code);
}
