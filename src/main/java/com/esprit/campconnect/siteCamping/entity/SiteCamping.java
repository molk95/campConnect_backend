package com.esprit.campconnect.siteCamping.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class SiteCamping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSite;

    String nom;

    String localisation;

    int capacite;

    double prixParNuit;

    @Enumerated(EnumType.STRING)
    StatutDispo statutDispo;

    /*@OneToMany(mappedBy = "siteCamping")
    Set<Avis> avis;*/

   /* @OneToMany(mappedBy = "siteCamping")
    Set<InscriptionSite> inscriptions;*/
}
