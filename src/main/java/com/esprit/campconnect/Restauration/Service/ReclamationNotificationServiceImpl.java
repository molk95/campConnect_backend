package com.esprit.campconnect.Restauration.Service;

import com.esprit.campconnect.Reclamation.DTO.UnreadCountDTO;
import com.esprit.campconnect.Restauration.DTO.CommandeRepasNotificationDTO;
import com.esprit.campconnect.Restauration.Entity.CommandeRepas;
import com.esprit.campconnect.Restauration.Entity.CommandeRepasNotification;
import com.esprit.campconnect.Restauration.Repository.CommandeRepasNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommandeRepasNotificationServiceImpl
        implements ICommandeRepasNotificationService {

    private final CommandeRepasNotificationRepository notifRepo;

    @Override
    public void createNotification(CommandeRepas commande, Long gerantId) {
        CommandeRepasNotification notif = CommandeRepasNotification.builder()
                .commande(commande)
                .destinataireId(gerantId)
                .message("🍽 New meal order #" + commande.getId() +
                        " — please prepare it.")
                .statut(commande.getStatut())
                .dateCreation(LocalDateTime.now())
                .read(false)
                .build();

        notifRepo.save(notif);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandeRepasNotificationDTO> getMyNotifications(Long gerantId) {
        return notifRepo
                .findByDestinataireidOrderByDateCreationDesc(gerantId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountDTO getUnreadCount(Long gerantId) {
        return new UnreadCountDTO(
                notifRepo.countByDestinataireidAndReadFalse(gerantId));
    }

    @Override
    public CommandeRepasNotificationDTO markAsRead(Long notifId, Long gerantId) {
        CommandeRepasNotification notif = notifRepo
                .findByIdAndDestinataireid(notifId, gerantId)
                .orElseThrow(() -> new RuntimeException(
                        "Notification " + notifId + " not found"));
        notif.setRead(true);
        notif.setReadAt(LocalDateTime.now());
        return toDTO(notifRepo.save(notif));
    }

    @Override
    public void markAllAsRead(Long gerantId) {
        notifRepo.markAllAsReadByUserId(gerantId);
    }

    private CommandeRepasNotificationDTO toDTO(CommandeRepasNotification n) {
        return CommandeRepasNotificationDTO.builder()
                .id(n.getId())
                .commandeId(n.getCommande().getId())
                .message(n.getMessage())
                .statut(n.getStatut())
                .dateCreation(n.getDateCreation())
                .read(n.isRead())
                .readAt(n.getReadAt())
                .build();
    }
}