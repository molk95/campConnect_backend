package com.esprit.campconnect.MarketPlace.Produit.Entity;

import jakarta.persistence.*;


@Entity
@Table(name = "produit")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProduit;

    private String nom;

    private String description;

    private double prix;

    private int stock;

    private String image;

    @Enumerated(EnumType.STRING)
    private Categorie categorie;

    public Produit() {}

    public Long getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(Long idProduit) {
        this.idProduit = idProduit;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Categorie getcategorie() {
        return categorie;
    }

    public void setcategorie(Categorie categorie) {
        this.categorie = categorie;
    }
}
