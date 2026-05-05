package com.esprit.campconnect.Assurance.Service;


import com.esprit.campconnect.Assurance.Entity.Remboursement;
import com.esprit.campconnect.Assurance.Entity.Sinistre;
import com.esprit.campconnect.Assurance.Repository.RemboursementRepository;
import com.esprit.campconnect.Assurance.Repository.SinistreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import com.esprit.campconnect.Assurance.Entity.StatutRemboursement;
import com.esprit.campconnect.Assurance.Entity.StatutSinistre;
import com.esprit.campconnect.Mail.Service.IMailService;
import com.esprit.campconnect.Notification.Entity.NotificationType;
import com.esprit.campconnect.Notification.Service.INotificationUserService;
import com.esprit.campconnect.User.Entity.Utilisateur;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RemboursementServiceImp implements IRemboursementService {

    private final RemboursementRepository remboursementRepository;
    private final SinistreRepository sinistreRepository;

    private final INotificationUserService notificationUserService;
    private final IMailService mailService;

    @Override
    public List<Remboursement> retrieveAll() {
        return remboursementRepository.findAll();
    }

    @Override
    public Remboursement retrieveById(Long id) {
        return remboursementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Remboursement introuvable"));
    }

    @Override
    public List<Remboursement> retrieveBySinistre(Long sinistreId) {
        return remboursementRepository.findBySinistreId(sinistreId);
    }

    @Override
    public Remboursement add(Long sinistreId, Remboursement remboursement) {
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));

        remboursement.setSinistre(sinistre);
        remboursement.setDateRemboursement(LocalDate.now());
        remboursement.setMontant(sinistre.getMontantRembourse());
        remboursement.setStatut(StatutRemboursement.EFFECTUE);

        if (remboursement.getMotif() == null || remboursement.getMotif().isBlank()) {
            remboursement.setMotif("Remboursement assurance validé.");
        }

        Remboursement saved = remboursementRepository.save(remboursement);

        sinistre.setStatut(StatutSinistre.REMBOURSE);
        sinistreRepository.save(sinistre);

        envoyerNotificationEtMailRemboursement(sinistre, saved);

        return saved;
    }

    private void envoyerNotificationEtMailRemboursement(Sinistre sinistre, Remboursement remboursement) {

        System.out.println("🔔 Début notification remboursement");

        if (sinistre.getSouscriptionAssurance() == null) {
            System.out.println("❌ Souscription null");
            return;
        }

        if (sinistre.getSouscriptionAssurance().getUtilisateur() == null) {
            System.out.println("❌ Utilisateur null");
            return;
        }

        Utilisateur utilisateur = sinistre.getSouscriptionAssurance().getUtilisateur();

        System.out.println("✅ Utilisateur trouvé : " + utilisateur.getId() + " - " + utilisateur.getEmail());

        String titre = "Remboursement assurance validé";
        String message = "Votre sinistre a été remboursé avec succès. Montant remboursé : "
                + remboursement.getMontant() + " TND.";

        notificationUserService.createNotification(
                utilisateur,
                titre,
                message,
                NotificationType.ASSURANCE_REMBOURSEMENT
        );

        System.out.println("✅ Notification créée");

        try {
            if (utilisateur.getEmail() != null && !utilisateur.getEmail().isBlank()) {
                String contrat = sinistre.getSouscriptionAssurance().getNumeroContrat();

                String body = "Bonjour " + utilisateur.getNom() + ",\n\n"
                        + "Votre sinistre a été remboursé avec succès.\n\n"
                        + "Montant remboursé : " + remboursement.getMontant() + " TND\n"
                        + "Contrat : " + (contrat != null ? contrat : "-") + "\n"
                        + "Date de remboursement : " + remboursement.getDateRemboursement() + "\n\n"
                        + "Cordialement,\n"
                        + "CampConnect";

                mailService.sendMail(
                        utilisateur.getEmail(),
                        "Remboursement assurance validé - CampConnect",
                        body
                );

                System.out.println("✅ Mail envoyé");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur mail, mais notification déjà créée : " + e.getMessage());
        }
    }

    @Override
    public Remboursement update(Remboursement remboursement) {
        Remboursement existing = remboursementRepository.findById(remboursement.getId())
                .orElseThrow(() -> new RuntimeException("Remboursement introuvable"));

        existing.setDateRemboursement(remboursement.getDateRemboursement());
        existing.setMontant(remboursement.getMontant());
        existing.setStatut(remboursement.getStatut());
        existing.setMotif(remboursement.getMotif());

        return remboursementRepository.save(existing);
    }

    @Override
    public void remove(Long id) {
        remboursementRepository.deleteById(id);
    }
}
