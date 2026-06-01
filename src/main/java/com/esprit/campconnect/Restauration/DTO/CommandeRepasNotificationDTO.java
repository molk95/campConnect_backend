package com.esprit.campconnect.Restauration.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeRepasNotificationDTO {
    private Long id;
    private Long commandeId;
    private String message;
    private String statut;
    private LocalDateTime dateCreation;
    private boolean read;
    private LocalDateTime readAt;
}