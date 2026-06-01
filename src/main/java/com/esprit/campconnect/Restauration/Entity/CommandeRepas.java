package com.esprit.campconnect.Restauration.Entity;
import com.esprit.campconnect.Restauration.Enum.StatutCommandeRepas;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CommandeRepas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateCommande;
    private double montantTotal;

    @Enumerated(EnumType.STRING)
    private StatutCommandeRepas statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "commandeRepas", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<LigneCommandeRepas> lignes = new ArrayList<>();

    // 🔥 Méthode utilitaire (TRÈS IMPORTANT)
    public void addLigne(LigneCommandeRepas ligne) {
        lignes.add(ligne);
        ligne.setCommandeRepas(this);
    }

    public void removeLigne(LigneCommandeRepas ligne) {
        lignes.remove(ligne);
        ligne.setCommandeRepas(null);
    }

}
