package com.esprit.campconnect.MarketPlace.Panier.Repository;

import com.esprit.campconnect.MarketPlace.Panier.Entity.Panier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PanierRepository extends JpaRepository<Panier, Long> {
}

