package com.esprit.campconnect.InscriptionSite.service;

import com.esprit.campconnect.InscriptionSite.dto.InscriptionCheckoutResponse;
import com.esprit.campconnect.InscriptionSite.dto.InscriptionSiteCreateRequest;
import com.esprit.campconnect.InscriptionSite.dto.InscriptionSiteResponse;
import com.esprit.campconnect.InscriptionSite.dto.InscriptionSiteUpdateRequest;
import com.esprit.campconnect.InscriptionSite.entity.InscriptionSite;

import java.util.List;

public interface IInscriptionSiteService {

    InscriptionCheckoutResponse addInscriptionSite(InscriptionSiteCreateRequest request);
    InscriptionSiteResponse confirmPayment(Long idInscription);

    InscriptionSiteResponse patchInscriptionSite(Long idInscription, InscriptionSiteUpdateRequest request);

    InscriptionSiteResponse getInscriptionSiteById(Long idInscription);

    List<InscriptionSiteResponse> getAllInscriptionSites();

    void deleteInscriptionSite(Long idInscription);

    List<InscriptionSiteResponse> getBySiteCamping(Long idSite);


    InscriptionSiteResponse cancelInscriptionSite(Long idInscription);

    List<InscriptionSiteResponse> getMyInscriptions();

    byte[] generateTicket(Long idInscription);

    List<InscriptionSiteResponse> getMyCampBookingList();


}
