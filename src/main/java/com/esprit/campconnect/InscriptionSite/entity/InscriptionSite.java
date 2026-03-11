package com.esprit.campconnect.InscriptionSite.entity;

import com.esprit.campconnect.siteCamping.entity.SiteCamping;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)

@Entity
public class InscriptionSite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @jakarta.persistence.Id
    Long idInscription;

    LocalDate dateDebut;

    LocalDate  dateFin;
    int numberOfGuests;
    @Enumerated(EnumType.STRING)
    StatutInscription statut;

   @ManyToOne
   SiteCamping siteCamping;
}
