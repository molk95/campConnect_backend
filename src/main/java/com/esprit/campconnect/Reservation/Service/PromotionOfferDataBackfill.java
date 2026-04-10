package com.esprit.campconnect.Reservation.Service;

import com.esprit.campconnect.Reservation.Repository.PromotionOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionOfferDataBackfill implements ApplicationRunner {

    private final PromotionOfferRepository promotionOfferRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int updatedPromotions = promotionOfferRepository.backfillLegacyGlobalPromotions();
        if (updatedPromotions > 0) {
            log.info("Backfilled {} legacy promotions to global scope", updatedPromotions);
        }
    }
}
