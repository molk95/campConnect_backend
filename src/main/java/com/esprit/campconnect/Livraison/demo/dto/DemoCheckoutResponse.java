package com.esprit.campconnect.Livraison.demo.dto;

import com.esprit.campconnect.Livraison.entity.TypeCommandeLivraison;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DemoCheckoutResponse {
    Long commandeId;
    TypeCommandeLivraison typeCommande;
    Double total;
    String adresseLivraison;
    String noteLivraison;
    String statut;
}