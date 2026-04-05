package com.esprit.campconnect.MarketPlace.Produit.Service;

import com.esprit.campconnect.MarketPlace.Produit.Entity.Categorie;
import com.esprit.campconnect.MarketPlace.Produit.Entity.Produit;
import com.esprit.campconnect.MarketPlace.Produit.Entity.StockProduit;
import com.esprit.campconnect.MarketPlace.Produit.Repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;

    public ProduitServiceImpl(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    @Override
    public Produit ajouterProduit(Produit produit) {
        lierEtValiderStocks(produit);
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
        existingProduit.setImages(produit.getImages());
        existingProduit.setCategorie(produit.getCategorie());
        existingProduit.setActive(produit.isActive());

        existingProduit.getStocks().clear();

        if (produit.getStocks() != null) {
            for (StockProduit stock : produit.getStocks()) {
                StockProduit nouveauStock = new StockProduit();
                nouveauStock.setTaille(stock.getTaille());
                nouveauStock.setPointure(stock.getPointure());
                nouveauStock.setStock(stock.getStock());
                nouveauStock.setProduit(existingProduit);
                existingProduit.getStocks().add(nouveauStock);
            }
        }

        validerStocks(existingProduit);

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

    @Override
    public void desactiverProduit(Long id) {
        Produit produit = getProduitById(id);
        produit.setActive(false);
        produitRepository.save(produit);
    }

    @Override
    public void activerProduit(Long id) {
        Produit produit = getProduitById(id);
        produit.setActive(true);
        produitRepository.save(produit);
    }

    private void lierEtValiderStocks(Produit produit) {
        if (produit.getStocks() == null) {
            produit.setStocks(new ArrayList<>());
        }

        for (StockProduit stock : produit.getStocks()) {
            stock.setProduit(produit);
        }

        validerStocks(produit);
    }
    private void validerStocks(Produit produit) {

        if (produit.getCategorie() == Categorie.VETEMENT) {
            for (StockProduit stock : produit.getStocks()) {
                if (stock.getTaille() == null || stock.getTaille().isBlank()) {
                    throw new RuntimeException("La taille est obligatoire pour un vêtement");
                }
                stock.setPointure(null);
            }
        }

        if (produit.getCategorie() == Categorie.CHAUSSURE) {
            for (StockProduit stock : produit.getStocks()) {
                if (stock.getPointure() == null) {
                    throw new RuntimeException("La pointure est obligatoire pour une chaussure");
                }
                stock.setTaille(null);
            }
        }

        // 🔥 AJOUT ICI : calcul du stock total
        int total = 0;

        if (produit.getStocks() != null) {
            for (StockProduit stock : produit.getStocks()) {
                total += stock.getStock();
            }
        }

        produit.setStock(total); // ✅ STOCK GLOBAL
    }
}
