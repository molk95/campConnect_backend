package com.esprit.campconnect.Restauration.Entity;

import com.esprit.campconnect.Restauration.Enum.StatutCommandeRepas;
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
    @Enumerated(EnumType.STRING)
    private StatutCommandeRepas statut;         // statut de la commande au moment de la notif

    private LocalDateTime dateCreation;

    @Builder.Default
    @Column(name = "is_read")
    private boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}