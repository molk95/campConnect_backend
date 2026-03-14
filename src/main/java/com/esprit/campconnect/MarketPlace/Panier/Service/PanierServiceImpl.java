package com.esprit.campconnect.MarketPlace.Panier.Service;

import com.esprit.campconnect.MarketPlace.Panier.Entity.Panier;
import com.esprit.campconnect.MarketPlace.Panier.Repository.PanierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PanierServiceImpl implements PanierService {

    private final PanierRepository panierRepository;

    public PanierServiceImpl(PanierRepository panierRepository) {
        this.panierRepository = panierRepository;
    }

    @Override
    public Panier ajouterPanier(Panier panier) {
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
        existingPanier.setUtilisateur(panier.getUtilisateur());

        return panierRepository.save(existingPanier);
    }

    @Override
    public void deletePanier(Long id) {
        Panier panier = getPanierById(id);
        panierRepository.delete(panier);
    }
}

