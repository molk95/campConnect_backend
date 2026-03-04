package com.esprit.campconnect.InscriptionSite;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Gestion Inscription Site")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inscriptionsite")
public class InscriptionSiteController {
    private final IInscriptionSiteService iInscriptionSiteService;

    @Operation(description = "Récupérer une inscription site")
    @GetMapping("/get/{idInscription}")
    public InscriptionSite getInscriptionSite(@PathVariable Long idInscription) {
        return iInscriptionSiteService.getInscriptionSiteById(idInscription);
    }

    @Operation(description = "Ajouter une inscription site")
    @PostMapping("/add")
    public InscriptionSite addInscriptionSite(@RequestBody InscriptionSite i) {
        return iInscriptionSiteService.addInscriptionSite(i);
    }

    @Operation(description = "Supprimer une inscription site")
    @DeleteMapping("/delete/{idInscription}")
    public void deleteInscriptionSite(@PathVariable Long idInscription) {
        iInscriptionSiteService.deleteInscriptionSite(idInscription);
    }

    @Operation(description = "Récupérer toutes les inscriptions site")
    @GetMapping("/getAll")
    public List<InscriptionSite> getAll() {
        return iInscriptionSiteService.getAllInscriptionSites();
    }

    @Operation(description = "Mise à jour partielle d'une inscription site")
    @PatchMapping("/update/{idInscription}")
    public InscriptionSite patchInscriptionSite(
            @PathVariable Long idInscription,
            @RequestBody InscriptionSite updatedData) {

        return iInscriptionSiteService.patchInscriptionSite(idInscription, updatedData);
    }

    // optional endpoint
    @Operation(description = "Récupérer les inscriptions d'un site camping")
    @GetMapping("/bySite/{idSite}")
    public List<InscriptionSite> getBySite(@PathVariable Long idSite) {
        return iInscriptionSiteService.getBySiteCamping(idSite);
    }
}
