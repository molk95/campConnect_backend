package com.esprit.campconnect.Restauration.Repository;

import com.esprit.campconnect.Restauration.Entity.Repas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepasRepository extends JpaRepository<Repas, Long> {
    List<Repas> findByUtilisateurId(Long utilisateurId);
}