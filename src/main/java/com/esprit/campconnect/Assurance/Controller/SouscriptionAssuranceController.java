package com.esprit.campconnect.Assurance.Controller;


import com.esprit.campconnect.Assurance.Entity.SouscriptionAssurance;
import com.esprit.campconnect.Assurance.Service.ISouscriptionAssuranceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.esprit.campconnect.Assurance.DTO.AssuranceCheckoutSessionResponseDTO;
import com.esprit.campconnect.Assurance.Entity.StatutSouscription;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/souscription-assurance")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SouscriptionAssuranceController {

    private final ISouscriptionAssuranceService souscriptionService;

    @GetMapping("/all")
    public List<SouscriptionAssurance> getAll() {
        return souscriptionService.retrieveAll();
    }

    @GetMapping("/{id}")
    public SouscriptionAssurance getById(@PathVariable Long id) {
        return souscriptionService.retrieveById(id);
    }

    @GetMapping("/user/{utilisateurId}")
    public List<SouscriptionAssurance> getByUtilisateur(@PathVariable Long utilisateurId) {
        return souscriptionService.retrieveByUtilisateur(utilisateurId);
    }

    @PostMapping("/add/{utilisateurId}/{assuranceId}")
    public SouscriptionAssurance add(@PathVariable Long utilisateurId,
                                     @PathVariable Long assuranceId,
                                     @RequestBody SouscriptionAssurance souscription) {
        return souscriptionService.add(utilisateurId, assuranceId, souscription);
    }

    @PutMapping("/update")
    public SouscriptionAssurance update(@RequestBody SouscriptionAssurance souscription) {
        return souscriptionService.update(souscription);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        souscriptionService.remove(id);
    }

    @PostMapping("/add/reservation/{utilisateurId}/{assuranceId}/{reservationId}")
    public SouscriptionAssurance addForReservation(@PathVariable Long utilisateurId,
                                                   @PathVariable Long assuranceId,
                                                   @PathVariable Long reservationId,
                                                   @RequestBody SouscriptionAssurance souscription) {
        return souscriptionService.addForReservation(utilisateurId, assuranceId, reservationId, souscription);
    }

    @PostMapping("/add/inscription-site/{utilisateurId}/{assuranceId}/{inscriptionSiteId}")
    public SouscriptionAssurance addForInscriptionSite(@PathVariable Long utilisateurId,
                                                       @PathVariable Long assuranceId,
                                                       @PathVariable Long inscriptionSiteId,
                                                       @RequestBody SouscriptionAssurance souscription) {
        return souscriptionService.addForInscriptionSite(utilisateurId, assuranceId, inscriptionSiteId, souscription);
    }

    @PostMapping("/{souscriptionId}/checkout-session")
    public AssuranceCheckoutSessionResponseDTO createCheckoutSession(@PathVariable Long souscriptionId) {
        return souscriptionService.createCheckoutSession(souscriptionId);
    }

    @PostMapping("/checkout-session/sync")
    public SouscriptionAssurance syncCheckoutSession(@RequestBody Map<String, String> body) {
        return souscriptionService.syncCheckoutSession(body.get("sessionId"));
    }

    @PutMapping("/{id}/statut")
    public SouscriptionAssurance updateStatut(@PathVariable Long id,
                                              @RequestParam StatutSouscription statut) {
        return souscriptionService.updateStatut(id, statut);
    }


}