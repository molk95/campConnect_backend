package com.esprit.campconnect.siteCamping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Gestion Site Camping")
@RestController
@RequestMapping("/api/site-camping")
@AllArgsConstructor
public class SiteCampingController {
    private final ISiteCampingService iSiteCampingService;

    @Operation(description = "Récupérer un site camping")
    @GetMapping("/getsite/{idSite}")
    public SiteCamping retrieveSiteCamping(@PathVariable Long idSite) {
        return iSiteCampingService.getSiteCampingById(idSite);
    }

    @Operation(description = "Ajouter un site camping")
    @PostMapping("/addSite")
    public SiteCamping addSiteCamping(@RequestBody SiteCamping s) {
        return iSiteCampingService.addSiteCamping(s);
    }

    @Operation(description = "Supprimer un site camping")
    @DeleteMapping("/deleteSite/{idSite}")
    public void deleteSiteCamping(@PathVariable Long idSite) {
        iSiteCampingService.deleteSiteCamping(idSite);
    }

    @Operation(description = "Récupérer tous les sites camping")
    @GetMapping("/getAll")
    public List<SiteCamping> getAllSiteCampings() {
        return iSiteCampingService.getAllSiteCampings();
    }

    @Operation(description = "Mise à jour d'un site camping")
    @PatchMapping("/updateSite/{idSite}")
    public SiteCamping patchSiteCamping(
            @PathVariable Long idSite,
            @RequestBody SiteCamping updatedData) {

        return iSiteCampingService.patchSiteCamping(idSite, updatedData);
    }
}
