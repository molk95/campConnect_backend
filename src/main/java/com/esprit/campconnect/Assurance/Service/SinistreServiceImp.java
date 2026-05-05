package com.esprit.campconnect.Assurance.Service;


import com.esprit.campconnect.Assurance.Entity.Sinistre;
import com.esprit.campconnect.Assurance.Entity.SouscriptionAssurance;
import com.esprit.campconnect.Assurance.Repository.SinistreRepository;
import com.esprit.campconnect.Assurance.Repository.SouscriptionAssuranceRepository;
import com.esprit.campconnect.Reclamation.Entity.Reclamation;
import com.esprit.campconnect.Reclamation.Repository.ReclamationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.esprit.campconnect.Mail.Service.IMailService;
import com.esprit.campconnect.Notification.Entity.NotificationType;
import com.esprit.campconnect.Notification.Service.INotificationUserService;
import com.esprit.campconnect.User.Entity.Utilisateur;

import com.esprit.campconnect.Assurance.Entity.*;
import com.esprit.campconnect.Assurance.Repository.GarantieRepository;

import java.time.LocalDate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SinistreServiceImp implements ISinistreService {

    private final SinistreRepository sinistreRepository;
    private final SouscriptionAssuranceRepository souscriptionRepository;
    private final ReclamationRepository reclamationRepository;

    private final GarantieRepository garantieRepository;

    private final INotificationUserService notificationUserService;
    private final IMailService mailService;

    @Override
    public List<Sinistre> retrieveAll() {
        return sinistreRepository.findAll();
    }

    @Override
    public Sinistre retrieveById(Long id) {
        return sinistreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));
    }

    @Override
    public List<Sinistre> retrieveBySouscription(Long souscriptionId) {
        return sinistreRepository.findBySouscriptionAssuranceId(souscriptionId);
    }

    @Override
    public Sinistre add(Long souscriptionId, Sinistre sinistre) {
        SouscriptionAssurance souscription = souscriptionRepository.findById(souscriptionId)
                .orElseThrow(() -> new RuntimeException("Souscription introuvable"));

        prepareSinistreMetier(souscription, sinistre);

        return sinistreRepository.save(sinistre);
    }

    @Override
    public Sinistre addFromReclamation(Long souscriptionId, Long reclamationId, Sinistre sinistre) {

        SouscriptionAssurance souscription = souscriptionRepository.findById(souscriptionId)
                .orElseThrow(() -> new RuntimeException("Souscription introuvable"));

        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable"));

        prepareSinistreMetier(souscription, sinistre);
        sinistre.setReclamation(reclamation);

        return sinistreRepository.save(sinistre);
    }

    private void prepareSinistreMetier(SouscriptionAssurance souscription, Sinistre sinistre) {

        if (souscription.getStatut() != StatutSouscription.ACTIVE) {
            throw new RuntimeException("Vous ne pouvez déclarer un sinistre que pour une souscription active.");
        }

        LocalDate today = LocalDate.now();

        if (souscription.getDateDebut() != null && today.isBefore(souscription.getDateDebut())) {
            throw new RuntimeException("La période de couverture n'a pas encore commencé.");
        }

        if (souscription.getDateFin() != null && today.isAfter(souscription.getDateFin())) {
            souscription.setStatut(StatutSouscription.EXPIREE);
            souscriptionRepository.save(souscription);
            throw new RuntimeException("Cette souscription est expirée.");
        }

        sinistre.setSouscriptionAssurance(souscription);
        sinistre.setDateDeclaration(today);
        sinistre.setStatut(StatutSinistre.EN_ATTENTE);

        double montantRembourse = calculerMontantRembourse(souscription, sinistre.getMontantEstime());
        sinistre.setMontantRembourse(montantRembourse);
    }

    private double calculerMontantRembourse(SouscriptionAssurance souscription, double montantEstime) {

        if (souscription.getAssurance() == null) {
            return 0;
        }

        List<Garantie> garanties = garantieRepository.findByAssuranceId(
                souscription.getAssurance().getId()
        );

        if (garanties.isEmpty()) {
            return 0;
        }

        double meilleurRemboursement = 0;

        for (Garantie garantie : garanties) {
            double taux = garantie.getTauxRemboursement();

            if (taux <= 0) {
                taux = 70;
            }

            double montantCalcule = montantEstime * taux / 100;
            double montantApresFranchise = montantCalcule - garantie.getFranchise();

            if (montantApresFranchise < 0) {
                montantApresFranchise = 0;
            }

            double montantFinal = Math.min(montantApresFranchise, garantie.getPlafond());

            if (montantFinal > meilleurRemboursement) {
                meilleurRemboursement = montantFinal;
            }
        }

        return meilleurRemboursement;
    }



    @Override
    public Sinistre update(Sinistre sinistre) {
        Sinistre existing = sinistreRepository.findById(sinistre.getId())
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));

        existing.setDateDeclaration(sinistre.getDateDeclaration());
        existing.setTypeSinistre(sinistre.getTypeSinistre());
        existing.setDescription(sinistre.getDescription());
        existing.setLieuIncident(sinistre.getLieuIncident());
        existing.setMontantEstime(sinistre.getMontantEstime());
        existing.setMontantRembourse(sinistre.getMontantRembourse());
        StatutSinistre oldStatut = existing.getStatut();

        existing.setStatut(sinistre.getStatut());

        Sinistre saved = sinistreRepository.save(existing);

        if (oldStatut != StatutSinistre.REMBOURSE && saved.getStatut() == StatutSinistre.REMBOURSE) {
            envoyerNotificationRemboursement(saved);
        }

        if (oldStatut != StatutSinistre.EN_COURS && saved.getStatut() == StatutSinistre.EN_COURS) {
            envoyerNotificationSinistreEnCours(saved);
        }

        return saved;
    }

    private void envoyerNotificationRemboursement(Sinistre sinistre) {
        if (sinistre.getSouscriptionAssurance() == null ||
                sinistre.getSouscriptionAssurance().getUtilisateur() == null) {
            System.out.println("Notification impossible : utilisateur introuvable");
            return;
        }

        Utilisateur utilisateur = sinistre.getSouscriptionAssurance().getUtilisateur();

        String titre = "Remboursement assurance validé";
        String message = "Votre sinistre a été remboursé avec succès. Montant remboursé : "
                + sinistre.getMontantRembourse() + " TND.";

        notificationUserService.createNotification(
                utilisateur,
                titre,
                message,
                NotificationType.ASSURANCE_REMBOURSEMENT
        );

        try {
            mailService.sendMail(
                    utilisateur.getEmail(),
                    "Remboursement assurance validé - CampConnect",
                    "Bonjour " + utilisateur.getNom() + ",\n\n"
                            + message + "\n\n"
                            + "Cordialement,\nCampConnect"
            );
        } catch (Exception e) {
            System.out.println("Erreur mail remboursement : " + e.getMessage());
        }
    }

    private void envoyerNotificationSinistreEnCours(Sinistre sinistre) {
        if (sinistre.getSouscriptionAssurance() == null ||
                sinistre.getSouscriptionAssurance().getUtilisateur() == null) {
            return;
        }

        var utilisateur = sinistre.getSouscriptionAssurance().getUtilisateur();

        String titre = "Sinistre en cours de traitement";
        String message = "Votre sinistre est maintenant en cours de traitement par notre équipe.";

        notificationUserService.createNotification(
                utilisateur,
                titre,
                message,
                NotificationType.ASSURANCE_SINISTRE_EN_COURS
        );

        try {
            mailService.sendMail(
                    utilisateur.getEmail(),
                    "Sinistre en cours de traitement - CampConnect",
                    "Bonjour " + utilisateur.getNom() + ",\n\n"
                            + message + "\n\n"
                            + "Cordialement,\nCampConnect"
            );
        } catch (Exception e) {
            System.out.println("Erreur mail sinistre en cours : " + e.getMessage());
        }
    }



    @Override
    public void remove(Long id) {
        sinistreRepository.deleteById(id);
    }
}