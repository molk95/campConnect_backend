package com.esprit.campconnect.Restauration.Entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LigneCommandeRepas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantite;
    private double prixUnitaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_repas_id")
    @JsonBackReference
    private CommandeRepas commandeRepas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repas_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Repas repas;
}
