package com.esprit.campconnect.MarketPlace.DetailCommande.Controller;

import com.esprit.campconnect.MarketPlace.DetailCommande.Entity.DetailCommande;
import com.esprit.campconnect.MarketPlace.DetailCommande.Service.DetailCommandeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/details-commandes")
public class DetailCommandeController {

    private final DetailCommandeService detailCommandeService;

    public DetailCommandeController(DetailCommandeService detailCommandeService) {
        this.detailCommandeService = detailCommandeService;
    }

    @PostMapping
    public DetailCommande ajouterDetailCommande(@RequestBody DetailCommande detailCommande) {
        return detailCommandeService.ajouterDetailCommande(detailCommande);
    }

    @GetMapping
    public List<DetailCommande> getAll() {
        return detailCommandeService.getAllDetailsCommande();
    }

    @GetMapping("/{id}")
    public DetailCommande getById(@PathVariable Long id) {
        return detailCommandeService.getDetailCommandeById(id);
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody DetailCommande detailCommande) {
        try {
            detailCommandeService.updateDetailCommande(id, detailCommande);
            return "Détail commande modifié avec succès";
        } catch (Exception e) {
            return "Échec de la modification du détail commande";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        try {
            detailCommandeService.deleteDetailCommande(id);
            return "Détail commande supprimé avec succès";
        } catch (Exception e) {
            return "Échec de la suppression du détail commande";
        }
    }

    @GetMapping("/commande/{idCommande}")
    public List<DetailCommande> getByCommande(@PathVariable Long idCommande) {
        return detailCommandeService.getDetailsByCommande(idCommande);
    }
}