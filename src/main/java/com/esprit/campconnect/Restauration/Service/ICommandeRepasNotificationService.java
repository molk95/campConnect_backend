package com.esprit.campconnect.Restauration.Service;

import com.esprit.campconnect.Restauration.DTO.CommandeRepasNotificationDTO;
import com.esprit.campconnect.Restauration.Entity.CommandeRepas;
import com.esprit.campconnect.Reclamation.DTO.UnreadCountDTO;
import java.util.List;

public interface ICommandeRepasNotificationService {
    void createNotification(CommandeRepas commande, Long gerantId);
    List<CommandeRepasNotificationDTO> getMyNotifications(Long gerantId);
    UnreadCountDTO getUnreadCount(Long gerantId);
    CommandeRepasNotificationDTO markAsRead(Long notifId, Long gerantId);
    void markAllAsRead(Long gerantId);
}