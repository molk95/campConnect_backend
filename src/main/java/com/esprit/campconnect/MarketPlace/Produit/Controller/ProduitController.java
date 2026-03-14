package com.esprit.campconnect.MarketPlace.Produit.Controller;

import com.esprit.campconnect.MarketPlace.Produit.Entity.Categorie;
import com.esprit.campconnect.MarketPlace.Produit.Entity.Produit;
import com.esprit.campconnect.MarketPlace.Produit.Service.ProduitService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produits")
@CrossOrigin("*")
public class ProduitController {

    private final ProduitService produitService;

    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    @PostMapping
    public String ajouterProduit(@RequestBody Produit produit) {
        try {
            produitService.ajouterProduit(produit);
            return "Produit ajouté avec succès";
        } catch (Exception e) {
            return "Échec de l'ajout du produit";
        }
    }



    @GetMapping
    public List<Produit> getAll() {
        return produitService.getAllProduits();
    }

    @GetMapping("/{id}")
    public Produit getById(@PathVariable Long id) {
        return produitService.getProduitById(id);
    }

    @PutMapping("/{id}")
    public Produit update(@PathVariable Long id, @RequestBody Produit produit) {
        return produitService.updateProduit(id, produit);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        produitService.deleteProduit(id);
        return "Produit supprimé avec succès";
    }

    @GetMapping("/categorie/{categorie}")
    public List<Produit> getByCategory(@PathVariable Categorie categorie) {
        return produitService.getProduitsByCategory(categorie);
    }
}
