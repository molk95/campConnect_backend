package com.esprit.campconnect.Restauration.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class RepasRequestDTO {
    private String nom;
    private double prix;
    private String image;
}
