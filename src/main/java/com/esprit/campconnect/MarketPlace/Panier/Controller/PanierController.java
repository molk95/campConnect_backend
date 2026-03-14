package com.esprit.campconnect.MarketPlace.Panier.Controller;

import com.esprit.campconnect.MarketPlace.Panier.Entity.Panier;
import com.esprit.campconnect.MarketPlace.Panier.Service.PanierService;

import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/paniers")
@CrossOrigin("*")
public class PanierController {

    private final PanierService panierService;

    public PanierController(PanierService panierService) {
        this.panierService = panierService;
    }

    @PostMapping
    public String ajouterPanier(@RequestBody Panier panier) {
        try {
            panierService.ajouterPanier(panier);
            return "Panier ajouté avec succès";
        } catch (Exception e) {
            return "Échec de l'ajout du panier";
        }
    }

    @GetMapping
    public List<Panier> getAll() {
        return panierService.getAllPaniers();
    }

    @GetMapping("/{id}")
    public Panier getById(@PathVariable Long id) {
        return panierService.getPanierById(id);
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody Panier panier) {
        try {
            panierService.updatePanier(id, panier);
            return "Panier modifié avec succès";
        } catch (Exception e) {
            return "Échec de la modification du panier";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        try {
            panierService.deletePanier(id);
            return "Panier supprimé avec succès";
        } catch (Exception e) {
            return "Échec de la suppression du panier";
        }
    }
}