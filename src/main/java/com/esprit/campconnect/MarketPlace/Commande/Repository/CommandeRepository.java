package com.esprit.campconnect.MarketPlace.Commande.Repository;

import com.esprit.campconnect.MarketPlace.Commande.Entity.Commande;
import com.esprit.campconnect.MarketPlace.Commande.Entity.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


    @Repository
    public interface CommandeRepository extends JpaRepository<Commande, Long> {

        List<Commande> findByUtilisateurId(Long utilisateurId);

        List<Commande> findByStatut(StatutCommande statut);

}
