package com.esprit.campconnect.Livraison.controller;

import com.esprit.campconnect.Livraison.dto.*;
import com.esprit.campconnect.Livraison.service.ILivraisonService;
import com.esprit.campconnect.User.Entity.Utilisateur;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livraisons")
@RequiredArgsConstructor
@Tag(name = "Gestion Livraison")
public class LivraisonController {

    private final ILivraisonService livraisonService;

    @PostMapping
    public LivraisonResponse createLivraison(@RequestBody LivraisonCreateRequest request) {
        return livraisonService.createLivraison(request);
    }

    @PatchMapping("/{idLivraison}/assign-livreur")
    public LivraisonResponse assignLivreur(
            @PathVariable Long idLivraison,
            @RequestBody AssignLivreurRequest request) {
        return livraisonService.assignLivreur(idLivraison, request.getLivreurId());
    }

    @PatchMapping("/{idLivraison}/status")
    public LivraisonResponse updateStatus(
            @PathVariable Long idLivraison,
            @RequestBody LivraisonStatusUpdateRequest request) {
        return livraisonService.updateStatus(idLivraison, request);
    }

    @GetMapping("/mine")
    public List<LivraisonResponse> getMyLivraisons() {
        return livraisonService.getMyLivraisons();
    }

    @GetMapping
    public List<LivraisonResponse> getAll() {
        return livraisonService.getAll();
    }

    @GetMapping("/mine/stats")
    public LivraisonStatsResponse getMyStats() {
        return livraisonService.getMyStats();
    }

    @GetMapping("/orders/classique/available")
    public List<AvailableOrderForLivraisonResponse> getAvailableClassicOrders() {
        return livraisonService.getAvailableClassicOrders();
    }

    @GetMapping("/orders/repas/available")
    public List<AvailableOrderForLivraisonResponse> getAvailableRepasOrders() {
        return livraisonService.getAvailableRepasOrders();
    }

    @GetMapping("/livreurs")
    public List<Utilisateur> getLivreurs() {
        return livraisonService.getLivreurs();
    }
}