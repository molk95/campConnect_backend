package com.esprit.campconnect.Assurance.Service;

import com.esprit.campconnect.Assurance.DTO.AssuranceCheckoutSessionResponseDTO;
import com.esprit.campconnect.Assurance.Entity.SouscriptionAssurance;
import com.esprit.campconnect.Assurance.Entity.StatutSouscription;

import java.util.List;

public interface ISouscriptionAssuranceService {

    List<SouscriptionAssurance> retrieveAll();

    SouscriptionAssurance retrieveById(Long id);

    List<SouscriptionAssurance> retrieveByUtilisateur(Long utilisateurId);

    SouscriptionAssurance add(Long utilisateurId, Long assuranceId, SouscriptionAssurance souscription);

    SouscriptionAssurance addForReservation(Long utilisateurId, Long assuranceId, Long reservationId, SouscriptionAssurance souscription);

    SouscriptionAssurance addForInscriptionSite(Long utilisateurId, Long assuranceId, Long inscriptionSiteId, SouscriptionAssurance souscription);

    SouscriptionAssurance update(SouscriptionAssurance souscription);

    void remove(Long id);

    AssuranceCheckoutSessionResponseDTO createCheckoutSession(Long souscriptionId);

    SouscriptionAssurance syncCheckoutSession(String sessionId);

    SouscriptionAssurance updateStatut(Long souscriptionId, StatutSouscription statut);
}