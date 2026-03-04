package com.esprit.campconnect.InscriptionSite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscriptionSiteRepository extends JpaRepository<InscriptionSite, Long> {

    // optional useful method
    List<InscriptionSite> findBySiteCamping_IdSite(Long idSite);
}
