package com.esprit.campconnect.Assurance.Service;

import com.esprit.campconnect.Assurance.Entity.Assurance;
import com.esprit.campconnect.Assurance.Entity.SouscriptionAssurance;
import com.esprit.campconnect.Assurance.Repository.AssuranceRepository;
import com.esprit.campconnect.Assurance.Repository.SouscriptionAssuranceRepository;
import com.esprit.campconnect.InscriptionSite.entity.InscriptionSite;
import com.esprit.campconnect.InscriptionSite.repository.InscriptionSiteRepository;
import com.esprit.campconnect.Reservation.Entity.Reservation;
import com.esprit.campconnect.Reservation.Repository.ReservationRepository;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import com.esprit.campconnect.Mail.Service.IMailService;
import com.esprit.campconnect.Notification.Entity.NotificationType;
import com.esprit.campconnect.Notification.Service.INotificationUserService;

import com.esprit.campconnect.config.StripeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.esprit.campconnect.Assurance.DTO.AssuranceCheckoutSessionResponseDTO;
import com.esprit.campconnect.Assurance.Entity.StatutSouscription;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.time.LocalDate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SouscriptionAssuranceServiceImp implements ISouscriptionAssuranceService {

    private final SouscriptionAssuranceRepository souscriptionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AssuranceRepository assuranceRepository;

    private final ReservationRepository reservationRepository;
    private final InscriptionSiteRepository inscriptionSiteRepository;
    private final INotificationUserService notificationUserService;
    private final IMailService mailService;

    private final StripeProperties stripeProperties;

    @Override
    public List<SouscriptionAssurance> retrieveAll() {
        return souscriptionRepository.findAll();
    }

    @Override
    public SouscriptionAssurance retrieveById(Long id) {
        return souscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Souscription introuvable"));
    }

    @Override
    public List<SouscriptionAssurance> retrieveByUtilisateur(Long utilisateurId) {
        return souscriptionRepository.findByUtilisateurId(utilisateurId);
    }

    @Override
    public SouscriptionAssurance add(Long utilisateurId, Long assuranceId, SouscriptionAssurance souscription) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Assurance assurance = assuranceRepository.findById(assuranceId)
                .orElseThrow(() -> new RuntimeException("Assurance introuvable"));

        prepareSouscriptionMetier(utilisateur, assurance, souscription);

        return souscriptionRepository.save(souscription);
    }

    @Override
    public SouscriptionAssurance addForReservation(Long utilisateurId, Long assuranceId, Long reservationId, SouscriptionAssurance souscription) {

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Assurance assurance = assuranceRepository.findById(assuranceId)
                .orElseThrow(() -> new RuntimeException("Assurance introuvable"));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

        prepareSouscriptionMetier(utilisateur, assurance, souscription);
        souscription.setReservation(reservation);

        return souscriptionRepository.save(souscription);
    }

    @Override
    public SouscriptionAssurance addForInscriptionSite(Long utilisateurId, Long assuranceId, Long inscriptionSiteId, SouscriptionAssurance souscription) {

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Assurance assurance = assuranceRepository.findById(assuranceId)
                .orElseThrow(() -> new RuntimeException("Assurance introuvable"));

        InscriptionSite inscriptionSite = inscriptionSiteRepository.findById(inscriptionSiteId)
                .orElseThrow(() -> new RuntimeException("Inscription site introuvable"));

        prepareSouscriptionMetier(utilisateur, assurance, souscription);
        souscription.setInscriptionSite(inscriptionSite);

        return souscriptionRepository.save(souscription);
    }

    @Override
    public SouscriptionAssurance update(SouscriptionAssurance souscription) {
        SouscriptionAssurance existing = souscriptionRepository.findById(souscription.getId())
                .orElseThrow(() -> new RuntimeException("Souscription introuvable"));

        existing.setNumeroContrat(souscription.getNumeroContrat());
        existing.setDateSouscription(souscription.getDateSouscription());
        existing.setDateDebut(souscription.getDateDebut());
        existing.setDateFin(souscription.getDateFin());
        existing.setStatut(souscription.getStatut());
        existing.setMontantPaye(souscription.getMontantPaye());
        existing.setBeneficiaireNom(souscription.getBeneficiaireNom());
        existing.setBeneficiaireTelephone(souscription.getBeneficiaireTelephone());

        return souscriptionRepository.save(existing);
    }

    @Override
    public void remove(Long id) {
        souscriptionRepository.deleteById(id);
    }


    @Override
    public AssuranceCheckoutSessionResponseDTO createCheckoutSession(Long souscriptionId) {
        try {
            SouscriptionAssurance souscription = retrieveById(souscriptionId);

            if (souscription.getAssurance() == null) {
                throw new RuntimeException("Assurance introuvable pour cette souscription");
            }

            Stripe.apiKey = stripeProperties.getSecretKey();

            long amount = Math.round(souscription.getAssurance().getPrime() * 100);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(stripeProperties.getFrontendBaseUrl()
                            + "/public/assurances/payment-success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(stripeProperties.getFrontendBaseUrl()
                            + "/public/assurances/payment-cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(stripeProperties.getCurrency())
                                                    .setUnitAmount(amount)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Assurance CampConnect - " + souscription.getAssurance().getTitre())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            souscription.setStripeSessionId(session.getId());
            souscription.setStripePaymentStatus("PENDING");
            souscription.setStatut(StatutSouscription.EN_ATTENTE);
            souscriptionRepository.save(souscription);

            return new AssuranceCheckoutSessionResponseDTO(session.getId(), session.getUrl());

        } catch (Exception e) {
            throw new RuntimeException("Erreur création session Stripe : " + e.getMessage());
        }
    }

    @Override
    public SouscriptionAssurance syncCheckoutSession(String sessionId) {
        try {
            Stripe.apiKey = stripeProperties.getSecretKey();

            Session session = Session.retrieve(sessionId);

            SouscriptionAssurance souscription = souscriptionRepository.findAll()
                    .stream()
                    .filter(s -> sessionId.equals(s.getStripeSessionId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Souscription introuvable pour cette session Stripe"));

            if ("paid".equals(session.getPaymentStatus())) {
                souscription.setStripePaymentStatus("PAID");
                souscription.setStatut(StatutSouscription.ACTIVE);

                if (souscription.getAssurance() != null) {
                    souscription.setMontantPaye(souscription.getAssurance().getPrime());
                }
            } else {
                souscription.setStripePaymentStatus(session.getPaymentStatus());
                souscription.setStatut(StatutSouscription.EN_ATTENTE);
            }

            return souscriptionRepository.save(souscription);

        } catch (Exception e) {
            throw new RuntimeException("Erreur synchronisation Stripe : " + e.getMessage());
        }
    }


    private void prepareSouscriptionMetier(
            Utilisateur utilisateur,
            Assurance assurance,
            SouscriptionAssurance souscription
    ) {
        if (!assurance.isActive()) {
            souscription.setStatut(StatutSouscription.REFUSEE);
            throw new RuntimeException("Cette assurance n'est pas active.");
        }

        boolean alreadyActive = souscriptionRepository.existsByUtilisateurIdAndAssuranceIdAndStatut(
                utilisateur.getId(),
                assurance.getId(),
                StatutSouscription.ACTIVE
        );

        if (alreadyActive) {
            souscription.setStatut(StatutSouscription.REFUSEE);
            throw new RuntimeException("Vous avez déjà une souscription active pour cette assurance.");
        }

        LocalDate today = LocalDate.now();

        souscription.setUtilisateur(utilisateur);
        souscription.setAssurance(assurance);
        souscription.setDateSouscription(today);

        if (souscription.getDateDebut() == null) {
            souscription.setDateDebut(today);
        }

        souscription.setDateFin(souscription.getDateDebut().plusDays(assurance.getDureeValidite()));
        souscription.setMontantPaye(assurance.getPrime());
        souscription.setNumeroContrat(generateNumeroContrat());
        souscription.setStatut(StatutSouscription.EN_ATTENTE);
        souscription.setStripePaymentStatus("PENDING");
    }

    private String generateNumeroContrat() {
        return "CC-ASS-" + System.currentTimeMillis();
    }

    @Override
    public SouscriptionAssurance updateStatut(Long souscriptionId, StatutSouscription statut) {
        SouscriptionAssurance souscription = souscriptionRepository.findById(souscriptionId)
                .orElseThrow(() -> new RuntimeException("Souscription introuvable"));

        StatutSouscription oldStatut = souscription.getStatut();

        souscription.setStatut(statut);

        if (statut == StatutSouscription.ACTIVE) {
            souscription.setStripePaymentStatus("MANUAL_VALIDATION");
        }

        SouscriptionAssurance saved = souscriptionRepository.save(souscription);

        if (oldStatut != StatutSouscription.ACTIVE && statut == StatutSouscription.ACTIVE) {
            envoyerNotificationSouscriptionAcceptee(saved);
        }

        return saved;
    }

    private void envoyerNotificationSouscriptionAcceptee(SouscriptionAssurance souscription) {
        if (souscription.getUtilisateur() == null) return;

        String titre = "Souscription acceptée";
        String message = "Votre souscription d’assurance a été acceptée. Contrat : "
                + souscription.getNumeroContrat();

        notificationUserService.createNotification(
                souscription.getUtilisateur(),
                titre,
                message,
                NotificationType.ASSURANCE_SOUSCRIPTION_ACCEPTEE
        );

        try {
            mailService.sendMail(
                    souscription.getUtilisateur().getEmail(),
                    "Souscription assurance acceptée - CampConnect",
                    "Bonjour " + souscription.getUtilisateur().getNom() + ",\n\n"
                            + message + "\n\n"
                            + "Cordialement,\nCampConnect"
            );
        } catch (Exception e) {
            System.out.println("Erreur mail souscription acceptée : " + e.getMessage());
        }
    }
}
