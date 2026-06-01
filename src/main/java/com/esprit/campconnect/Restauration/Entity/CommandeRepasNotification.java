package com.esprit.campconnect.Restauration.Entity;

import com.esprit.campconnect.User.Entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commande_repas_notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeRepasNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id")
    private CommandeRepas commande;

    // ID du gérant destinataire
    private Long destinataireId;

    private String message;
    private String statut;           // statut de la commande au moment de la notif

    private LocalDateTime dateCreation;

    @Builder.Default
    private boolean read = false;

    private LocalDateTime readAt;
}