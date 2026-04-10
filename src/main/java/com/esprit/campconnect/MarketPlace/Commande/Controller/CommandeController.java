package com.esprit.campconnect.MarketPlace.Commande.Controller;

import com.esprit.campconnect.MarketPlace.Commande.Entity.Commande;
import com.esprit.campconnect.MarketPlace.Commande.Entity.StatutCommande;
import com.esprit.campconnect.MarketPlace.Commande.Service.CommandeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commandes")
public class CommandeController {

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @PostMapping
    public String ajouterCommande(@RequestBody Commande commande) {
        try {
            commandeService.ajouterCommande(commande);
            return "Commande ajoutée avec succès";
        } catch (Exception e) {
            return "Échec de l'ajout de la commande";
        }
    }

    @GetMapping
    public List<Commande> getAll() {
        return commandeService.getAllCommandes();
    }

    @GetMapping("/{id}")
    public Commande getById(@PathVariable Long id) {
        return commandeService.getCommandeById(id);
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody Commande commande) {
        try {
            commandeService.updateCommande(id, commande);
            return "Commande modifiée avec succès";
        } catch (Exception e) {
            return "Échec de la modification de la commande";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        try {
            commandeService.deleteCommande(id);
            return "Commande supprimée avec succès";
        } catch (Exception e) {
            return "Échec de la suppression de la commande";
        }
    }

    @PutMapping("/{id}/statut")
    public Commande changerStatut(@PathVariable Long id, @RequestParam StatutCommande statut) {
        return commandeService.changerStatut(id, statut);
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public List<Commande> getByUtilisateur(@PathVariable Long utilisateurId) {
        return commandeService.getCommandesByUtilisateur(utilisateurId);
    }

    @GetMapping("/statut/{statut}")
    public List<Commande> getByStatut(@PathVariable StatutCommande statut) {
        return commandeService.getCommandesByStatut(statut);
    }

    @PostMapping("/panier/{idPanier}")
    public ResponseEntity<?> commanderDepuisPanier(@PathVariable Long idPanier) {
        try {
            Commande commande = commandeService.commanderDepuisPanier(idPanier);
            return ResponseEntity.ok(commande);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}