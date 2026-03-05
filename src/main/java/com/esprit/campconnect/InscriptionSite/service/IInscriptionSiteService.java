package com.esprit.campconnect.InscriptionSite.service;

import com.esprit.campconnect.InscriptionSite.entity.InscriptionSite;

import java.util.List;

public interface IInscriptionSiteService {
    InscriptionSite addInscriptionSite(InscriptionSite inscriptionSite);

    InscriptionSite patchInscriptionSite(Long idInscription, InscriptionSite updatedData);

    InscriptionSite getInscriptionSiteById(Long idInscription);

    List<InscriptionSite> getAllInscriptionSites();

    void deleteInscriptionSite(Long idInscription);

    // optional
    List<InscriptionSite> getBySiteCamping(Long idSite);
}
