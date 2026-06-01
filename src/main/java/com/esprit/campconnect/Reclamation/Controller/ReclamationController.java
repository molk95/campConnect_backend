package com.esprit.campconnect.Reclamation.Controller;

import com.esprit.campconnect.Reclamation.Entity.Reclamation;
import org.springframework.web.multipart.MultipartFile;
import com.esprit.campconnect.Reclamation.Entity.StatutReclamation;
import com.esprit.campconnect.Reclamation.Repository.ReclamationRepository;
import com.esprit.campconnect.Reclamation.Service.IReclamationNotificationService;
import com.esprit.campconnect.Reclamation.Service.ReclamationService;
import com.esprit.campconnect.Restauration.Service.RepasCloudinaryService;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;

import com.esprit.campconnect.common.EmailService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reclamations")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ReclamationController {

    private final ReclamationService reclamationService;
    private final ReclamationRepository reclamationRepository;
    private final IReclamationNotificationService notifService;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailService emailService;
    private final RepasCloudinaryService cloudinaryService;

    @PostMapping(consumes = {"multipart/form-data"})
    public Reclamation create(
            @RequestParam("description") String description,
            @RequestParam("acceptationDeclaration") boolean acceptationDeclaration,
            @RequestParam("utilisateurId") Long utilisateurId,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        Reclamation reclamation = new Reclamation();
        reclamation.setDescription(description);
        reclamation.setAcceptationDeclaration(acceptationDeclaration);
        reclamation.setDateAcceptation(acceptationDeclaration ? java.time.LocalDateTime.now() : null);
        reclamation.setUtilisateur(utilisateur);
        reclamation.setStatut(StatutReclamation.EN_COURS);

        if (image != null && !image.isEmpty()) {
            Map<String, String> uploadResult = cloudinaryService.uploadImage(image);
            reclamation.setImage(uploadResult.get("imageUrl"));
        }

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

    @PutMapping("/{id}/statut")
    public Reclamation changeStatut(@PathVariable Long id,
                                    @RequestParam StatutReclamation statut,
                                    @RequestParam(required = false) Integer reduction) {

        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réclamation introuvable"));

        StatutReclamation ancienStatut = reclamation.getStatut();

        reclamation.setStatut(statut);
        if (reduction != null) {
            reclamation.setReductionPourcentage(reduction);
        }
        reclamationRepository.save(reclamation);

        if (!ancienStatut.equals(statut)) {
            notifService.createNotification(reclamation, ancienStatut.name(), statut.name());

            try {
                String email = reclamation.getUtilisateur().getEmail();
                String subject = "Mise à jour de votre réclamation #" + id;
                String htmlBody = buildEmailHtml(id, statut.name(), reduction);
                emailService.sendHtmlEmail(email, subject, htmlBody, null, null);
            } catch (Exception e) {
                System.err.println("Erreur envoi email réclamation: " + e.getMessage());
            }
        }

        return reclamation;
    }

    // ── Consommer la réduction d'une réclamation ──────────────────────────
    @PutMapping("/{id}/consommer-reduction")
    public ResponseEntity<?> consommerReduction(@PathVariable Long id, Authentication authentication) {

        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réclamation introuvable"));

        // Vérifier que la réclamation appartient bien à l'utilisateur connecté
        String email = authentication.getName();
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));

        if (!reclamation.getUtilisateur().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cette réclamation ne vous appartient pas");
        }

        if (reclamation.getReductionPourcentage() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucune réduction disponible pour cette réclamation");
        }

        reclamation.setReductionPourcentage(null);
        reclamationRepository.save(reclamation);

        return ResponseEntity.ok(Map.of("message", "Réduction consommée avec succès"));
    }

    private String buildEmailHtml(Long reclamationId, String newStatut, Integer reduction) {
        String statutLabel = switch (newStatut) {
            case "EN_COURS" -> "<span style='color:#f59e0b'>en cours de traitement</span>";
            case "RESOLUE"  -> "<span style='color:#10b981'>resolue</span>";
            case "REJETEE"  -> "<span style='color:#ef4444'>rejetee</span>";
            default         -> "mise a jour";
        };

        String reductionBlock = "";
        if (reduction != null && reduction > 0) {
            reductionBlock = """
            <div style='margin-top:16px; padding:12px; background:#f0fdf4; border-radius:8px; border:1px solid #bbf7d0'>
                <p style='margin:0; color:#166534; font-weight:600'>
                    En compensation, vous beneficiez d'une reduction de <strong>%d%%</strong> sur votre prochain repas.
                </p>
            </div>
            """.formatted(reduction);
        }

        return """
        <div style='font-family:sans-serif; max-width:600px; margin:auto; padding:24px'>
            <h2 style='color:#1e293b'>Mise a jour de votre reclamation</h2>
            <p>Bonjour,</p>
            <p>Votre reclamation <strong>#%d</strong> est maintenant %s.</p>
            %s
            <p style='margin-top:24px; color:#64748b'>Merci de votre confiance,<br><strong>L equipe CampConnect</strong></p>
        </div>
        """.formatted(reclamationId, statutLabel, reductionBlock);
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