package com.esprit.campconnect.Restauration.DTO;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;
import com.esprit.campconnect.Restauration.Entity.Repas;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RepasResponseDTO {
    private Long id;
    private String nom;
    private double prix;
    private String image;
    private Long utilisateurId;
    private String utilisateurEmail;
}
