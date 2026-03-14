package com.esprit.campconnect.MarketPlace.Produit.Service;

import com.esprit.campconnect.MarketPlace.Produit.Entity.Categorie;
import com.esprit.campconnect.MarketPlace.Produit.Entity.Produit;
import com.esprit.campconnect.MarketPlace.Produit.Repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;

    public ProduitServiceImpl(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    @Override
    public Produit ajouterProduit(Produit produit) {
        return produitRepository.save(produit);
    }

    @Override
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }

    @Override
    public Produit getProduitById(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec id : " + id));
    }

    @Override
    public Produit updateProduit(Long id, Produit produit) {
        Produit existingProduit = getProduitById(id);

        existingProduit.setNom(produit.getNom());
        existingProduit.setDescription(produit.getDescription());
        existingProduit.setPrix(produit.getPrix());
        existingProduit.setStock(produit.getStock());
        existingProduit.setImage(produit.getImage());
        existingProduit.setcategorie(produit.getcategorie());

        return produitRepository.save(existingProduit);
    }

    @Override
    public void deleteProduit(Long id) {
        produitRepository.deleteById(id);
    }

    @Override
    public List<Produit> getProduitsByCategory(Categorie categorie) {
        return produitRepository.findByCategorie(categorie);
    }
}