package com.esprit.campconnect.MarketPlace.Panier.Service;

import com.esprit.campconnect.MarketPlace.Panier.Entity.EtatPanier;
import com.esprit.campconnect.MarketPlace.Panier.Entity.Panier;
import com.esprit.campconnect.MarketPlace.Panier.Repository.PanierRepository;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PanierServiceImpl implements PanierService {

    private final PanierRepository panierRepository;
    private final UtilisateurRepository utilisateurRepository;

    public PanierServiceImpl(PanierRepository panierRepository,
                             UtilisateurRepository utilisateurRepository) {
        this.panierRepository = panierRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public Panier ajouterPanier(Panier panier) {
        if (panier.getUtilisateur() == null || panier.getUtilisateur().getId() == null) {
            throw new RuntimeException("Utilisateur obligatoire");
        }

        Long userId = panier.getUtilisateur().getId();

        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec id : " + userId));

        panier.setUtilisateur(utilisateur);

        if (panier.getDateCreation() == null) {
            panier.setDateCreation(LocalDate.now());
        }

        if (panier.getEtat() == null) {
            panier.setEtat(EtatPanier.EN_COURS);
        }

        return panierRepository.save(panier);
    }

    @Override
    public List<Panier> getAllPaniers() {
        return panierRepository.findAll();
    }

    @Override
    public Panier getPanierById(Long id) {
        return panierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Panier non trouvé avec id : " + id));
    }

    @Override
    public Panier updatePanier(Long id, Panier panier) {
        Panier existingPanier = getPanierById(id);

        existingPanier.setDateCreation(panier.getDateCreation());
        existingPanier.setEtat(panier.getEtat());

        if (panier.getUtilisateur() != null && panier.getUtilisateur().getId() != null) {
            Utilisateur utilisateur = utilisateurRepository.findById(panier.getUtilisateur().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Utilisateur non trouvé avec id : " + panier.getUtilisateur().getId()));
            existingPanier.setUtilisateur(utilisateur);
        }

        return panierRepository.save(existingPanier);
    }

    @Override
    public void deletePanier(Long id) {
        Panier panier = getPanierById(id);
        panierRepository.delete(panier);
    }

    @Override
    public Panier getOrCreatePanierEnCours(Long utilisateurId) {
        return panierRepository
                .findByUtilisateurIdAndEtat(utilisateurId, EtatPanier.EN_COURS)
                .orElseGet(() -> {
                    Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

                    Panier panier = new Panier();
                    panier.setDateCreation(LocalDate.now());
                    panier.setEtat(EtatPanier.EN_COURS);
                    panier.setUtilisateur(utilisateur);

                    return panierRepository.save(panier);
                });
    }

}