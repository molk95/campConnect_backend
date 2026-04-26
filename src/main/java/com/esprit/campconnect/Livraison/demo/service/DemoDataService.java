package com.esprit.campconnect.Livraison.demo.service;

import com.esprit.campconnect.Livraison.demo.dto.DemoCheckoutRequest;
import com.esprit.campconnect.Livraison.demo.dto.DemoCheckoutResponse;
import com.esprit.campconnect.Livraison.demo.entity.DemoProduit;
import com.esprit.campconnect.Livraison.demo.entity.DemoRepas;
import com.esprit.campconnect.Livraison.entity.TypeCommandeLivraison;
import com.esprit.campconnect.MarketPlace.Commande.Entity.Commande;
import com.esprit.campconnect.MarketPlace.Commande.Entity.StatutCommande;
import com.esprit.campconnect.MarketPlace.Commande.Repository.CommandeRepository;
import com.esprit.campconnect.Restauration.Entity.CommandeRepas;
import com.esprit.campconnect.Restauration.Enum.StatutCommandeRepas;
import com.esprit.campconnect.Restauration.Repository.CommandeRepasRepository;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DemoDataService {

    private final CommandeRepository commandeRepository;
    private final CommandeRepasRepository commandeRepasRepository;
    private final UtilisateurRepository utilisateurRepository;

    private final List<DemoProduit> produits = List.of(
            new DemoProduit(1L, "Tent", 120.0),
            new DemoProduit(2L, "Sleeping Bag", 60.0),
            new DemoProduit(3L, "Camping Stove", 45.0)
    );

    private final List<DemoRepas> repas = List.of(
            new DemoRepas(1L, "Burger Menu", 25.0),
            new DemoRepas(2L, "Pizza", 30.0),
            new DemoRepas(3L, "Tacos", 20.0)
    );

    public List<DemoProduit> getProduits() {
        return produits;
    }

    public List<DemoRepas> getRepas() {
        return repas;
    }

    private DemoProduit findProduit(Long id) {
        return produits.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    private DemoRepas findRepas(Long id) {
        return repas.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Meal not found"));
    }

    public Commande createCommande(Double total) {
        Utilisateur client = utilisateurRepository.findById(19L)
                .orElseThrow(() -> new RuntimeException("Demo client not found"));

        Commande cmd = new Commande();
        cmd.setDateCommande(LocalDate.now());
        cmd.setTotalCommande(total);
        cmd.setStatut(StatutCommande.EN_ATTENTE);
        cmd.setUtilisateur(client);

        return commandeRepository.save(cmd);
    }

    public CommandeRepas createCommandeRepas(Double total) {
        Utilisateur client = utilisateurRepository.findById(19L)
                .orElseThrow(() -> new RuntimeException("Demo client not found"));

        CommandeRepas cmd = new CommandeRepas();
        cmd.setDateCommande(LocalDate.now());
        cmd.setMontantTotal(total);
        cmd.setStatut(StatutCommandeRepas.EN_ATTENTE);
        cmd.setUtilisateur(client);

        return commandeRepasRepository.save(cmd);
    }

    public DemoCheckoutResponse createClassicCheckout(DemoCheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Please select at least one product");
        }

        double total = request.getItems()
                .stream()
                .mapToDouble(item -> {
                    DemoProduit produit = findProduit(item.getId());
                    return produit.getPrix() * item.getQuantity();
                })
                .sum();

        Commande commande = createCommande(total);

        return new DemoCheckoutResponse(
                commande.getIdCommande(),
                TypeCommandeLivraison.CLASSIQUE,
                total,
                request.getAdresseLivraison(),
                request.getNoteLivraison(),
                commande.getStatut().name()
        );
    }

    public DemoCheckoutResponse createRepasCheckout(DemoCheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Please select at least one meal");
        }

        double total = request.getItems()
                .stream()
                .mapToDouble(item -> {
                    DemoRepas repasItem = findRepas(item.getId());
                    return repasItem.getPrix() * item.getQuantity();
                })
                .sum();

        CommandeRepas commandeRepas = createCommandeRepas(total);

        return new DemoCheckoutResponse(
                commandeRepas.getId(),
                TypeCommandeLivraison.REPAS,
                total,
                request.getAdresseLivraison(),
                request.getNoteLivraison(),
                commandeRepas.getStatut().name()
        );
    }
}