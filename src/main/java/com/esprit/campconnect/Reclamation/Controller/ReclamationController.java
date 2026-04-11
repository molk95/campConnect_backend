package com.esprit.campconnect.Reclamation.Controller;

import com.esprit.campconnect.Reclamation.Entity.Reclamation;
import com.esprit.campconnect.Reclamation.Entity.StatutReclamation;
import com.esprit.campconnect.Reclamation.Repository.ReclamationRepository;
import com.esprit.campconnect.Reclamation.Service.IReclamationNotificationService;
import com.esprit.campconnect.Reclamation.Service.ReclamationService;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/reclamations")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ReclamationController {

    private final ReclamationService reclamationService;
    private final ReclamationRepository reclamationRepository;
    private final IReclamationNotificationService notifService;
    private final UtilisateurRepository utilisateurRepository;





    @PostMapping
    public Reclamation create(@RequestBody Reclamation reclamation) {

        Long userId = reclamation.getUtilisateur().getId();

        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        reclamation.setUtilisateur(utilisateur);

        return reclamationService.createReclamation(reclamation);
    }




    @GetMapping
    public List<Reclamation> getAll() {
        return reclamationService.getAllReclamations();
    }

    @GetMapping("/{id}")
    public Reclamation getById(@PathVariable Long id) {
        return reclamationService.getReclamationById(id);
    }
    @GetMapping("/me")
    public List<Reclamation> getMyReclamationById(Authentication authentication) {

        String email = authentication.getName();

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return reclamationService.getReclamationsByUtilisateur(user.getId());
    }
    @PutMapping("/{id}")
    public Reclamation update(@PathVariable Long id, @RequestBody Reclamation reclamation) {
        return reclamationService.updateReclamation(id, reclamation);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        reclamationService.deleteReclamation(id);
    }

    // ✅ CORRECTION IMPORTANTE ici
    @PutMapping("/{id}/statut")
    public Reclamation changeStatut(@PathVariable Long id,
                                    @RequestParam StatutReclamation statut) {

        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réclamation introuvable"));

        StatutReclamation ancienStatut = reclamation.getStatut();

        reclamation.setStatut(statut);
        reclamationRepository.save(reclamation);

        if (!ancienStatut.equals(statut)) {
            notifService.createNotification(reclamation, ancienStatut.name(), statut.name());
        }

        return reclamation;
    }

    @GetMapping("/statut/{statut}")
    public List<Reclamation> getByStatut(@PathVariable StatutReclamation statut) {
        return reclamationService.getReclamationsByStatut(statut);
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public List<Reclamation> getByUtilisateur(@PathVariable Long utilisateurId) {
        return reclamationService.getReclamationsByUtilisateur(utilisateurId);
    }

    @GetMapping("/count/en-cours")
    public long countEnCours() {
        return reclamationService.countReclamationsEnCours();
    }
}