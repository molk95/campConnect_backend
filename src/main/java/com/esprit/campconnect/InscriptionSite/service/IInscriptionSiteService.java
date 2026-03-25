package com.esprit.campconnect.InscriptionSite.service;

import com.esprit.campconnect.InscriptionSite.dto.InscriptionSiteCreateRequest;
import com.esprit.campconnect.InscriptionSite.dto.InscriptionSiteUpdateRequest;
import com.esprit.campconnect.InscriptionSite.entity.InscriptionSite;

import java.util.List;

public interface IInscriptionSiteService {
    InscriptionSite addInscriptionSite(InscriptionSiteCreateRequest request);

    InscriptionSite patchInscriptionSite(Long idInscription, InscriptionSiteUpdateRequest request);

    InscriptionSite getInscriptionSiteById(Long idInscription);

    List<InscriptionSite> getAllInscriptionSites();

    void deleteInscriptionSite(Long idInscription);

    // optional
    List<InscriptionSite> getBySiteCamping(Long idSite);

    InscriptionSite confirmInscriptionSite(Long idInscription);
    InscriptionSite cancelInscriptionSite(Long idInscription);
}
