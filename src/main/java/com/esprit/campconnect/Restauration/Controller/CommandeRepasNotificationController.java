package com.esprit.campconnect.Restauration.Controller;

import com.esprit.campconnect.Reclamation.DTO.UnreadCountDTO;
import com.esprit.campconnect.Restauration.DTO.CommandeRepasNotificationDTO;
import com.esprit.campconnect.Restauration.Service.ICommandeRepasNotificationService;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/commande-repas-notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CommandeRepasNotificationController {

    private final ICommandeRepasNotificationService notifService;
    private final UtilisateurRepository utilisateurRepository;

    private Long getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();
        return utilisateurRepository.findByEmail(email)
                .map(Utilisateur::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/me")
    public List<CommandeRepasNotificationDTO> getMyNotifications(
            Authentication authentication) {
        return notifService.getMyNotifications(getCurrentUserId(authentication));
    }

    @GetMapping("/me/unread-count")
    public UnreadCountDTO getUnreadCount(Authentication authentication) {
        return notifService.getUnreadCount(getCurrentUserId(authentication));
    }

    @PutMapping("/{id}/read")
    public CommandeRepasNotificationDTO markAsRead(
            @PathVariable Long id, Authentication authentication) {
        return notifService.markAsRead(id, getCurrentUserId(authentication));
    }

    @PutMapping("/me/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        notifService.markAllAsRead(getCurrentUserId(authentication));
        return ResponseEntity.noContent().build();
    }
}