package com.esprit.campconnect.Livraison.repository;

import com.esprit.campconnect.Livraison.entity.Livraison;
import com.esprit.campconnect.Livraison.entity.StatutLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivraisonRepository extends JpaRepository<Livraison, Long> {
    List<Livraison> findByLivreur_Id(Long livreurId);

    long countByLivreur_IdAndStatutIn(Long livreurId, List<StatutLivraison> statuts);

    long countByLivreur_Id(Long livreurId);

    long countByLivreur_IdAndStatut(Long livreurId, StatutLivraison statut);
}