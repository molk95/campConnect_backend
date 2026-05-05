package com.esprit.campconnect.Assurance.Entity;


import com.esprit.campconnect.InscriptionSite.entity.InscriptionSite;
import com.esprit.campconnect.Reservation.Entity.Reservation;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class SouscriptionAssurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String numeroContrat;
    LocalDate dateSouscription;
    LocalDate dateDebut;
    LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    StatutSouscription statut;

    double montantPaye;
    String beneficiaireNom;
    String beneficiaireTelephone;

    String stripeSessionId;
    String stripePaymentStatus;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    @JsonIgnoreProperties({"motDePasse", "authorities", "profil"})
    Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "assurance_id")
    Assurance assurance;

    @OneToMany(mappedBy = "souscriptionAssurance", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Sinistre> sinistres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscription_site_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    InscriptionSite inscriptionSite;





}