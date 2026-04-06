package com.esprit.campconnect.Assurance.Repository;

import com.esprit.campconnect.Assurance.Entity.SouscriptionAssurance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SouscriptionAssuranceRepository extends JpaRepository<SouscriptionAssurance, Long> {
    List<SouscriptionAssurance> findByUtilisateurId(Long utilisateurId);
}
