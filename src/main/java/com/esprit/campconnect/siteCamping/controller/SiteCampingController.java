package com.esprit.campconnect.siteCamping.controller;

import com.esprit.campconnect.siteCamping.dto.SiteCampingCreateRequest;
import com.esprit.campconnect.siteCamping.dto.SiteCampingUpdateRequest;
import com.esprit.campconnect.siteCamping.service.ISiteCampingService;
import com.esprit.campconnect.siteCamping.entity.SiteCamping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Gestion Site Camping")
@RestController
@RequestMapping("/site-camping")
@AllArgsConstructor
public class SiteCampingController {
    private final ISiteCampingService iSiteCampingService;

    @Operation(description = "Récupérer un site camping")
    @GetMapping("/getsite/{idSite}")
    public SiteCamping retrieveSiteCamping(@PathVariable Long idSite) {
        return iSiteCampingService.getSiteCampingById(idSite);
    }

    @Operation(description = "Ajouter un site camping")
    @PostMapping(value = "/addSite", consumes = "multipart/form-data")
    public SiteCamping addSiteCamping(@ModelAttribute SiteCampingCreateRequest request) {
        return iSiteCampingService.addSiteCamping(request);
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
    @PatchMapping(value = "/updateSite/{idSite}", consumes = "multipart/form-data")
    public SiteCamping patchSiteCamping(
            @PathVariable Long idSite,
            @ModelAttribute SiteCampingUpdateRequest updatedData) {

        return iSiteCampingService.patchSiteCamping(idSite, updatedData);
    }
}
