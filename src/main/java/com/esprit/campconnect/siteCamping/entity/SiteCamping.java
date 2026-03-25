package com.esprit.campconnect.siteCamping.entity;

import com.esprit.campconnect.InscriptionSite.entity.InscriptionSite;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

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
    String imageUrl;
    String imagePublicId;
    String description;

    @Enumerated(EnumType.STRING)
    StatutDispo statutDispo;

    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "siteCamping")
    Set<InscriptionSite> inscriptions;
}
