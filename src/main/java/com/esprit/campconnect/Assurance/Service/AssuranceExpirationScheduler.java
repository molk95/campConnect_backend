package com.esprit.campconnect.Assurance.Service;

import com.esprit.campconnect.Assurance.Entity.SouscriptionAssurance;
import com.esprit.campconnect.Assurance.Entity.StatutSouscription;
import com.esprit.campconnect.Assurance.Repository.SouscriptionAssuranceRepository;
import com.esprit.campconnect.Mail.Service.IMailService;
import com.esprit.campconnect.Notification.Entity.NotificationType;
import com.esprit.campconnect.Notification.Service.INotificationUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssuranceExpirationScheduler {

    private final SouscriptionAssuranceRepository souscriptionRepository;
    private final INotificationUserService notificationUserService;
    private final IMailService mailService;

    //@Scheduled(cron = "0 0 * * * *")
    @Scheduled(fixedRate = 60000)
    public void notifierAssurancesQuiExpirent() {

        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(3);

        System.out.println("Scheduler assurance expiration lancé : " + today + " -> " + maxDate);

        List<SouscriptionAssurance> souscriptions =
                souscriptionRepository.findByStatutAndDateFinBetweenAndNotificationExpirationEnvoyeeFalse(
                        StatutSouscription.ACTIVE,
                        today,
                        maxDate
                );

        System.out.println("Souscriptions proches expiration trouvées : " + souscriptions.size());

        for (SouscriptionAssurance souscription : souscriptions) {

            if (souscription.getUtilisateur() == null) {
                continue;
            }

            long joursRestants = ChronoUnit.DAYS.between(today, souscription.getDateFin());

            String titre = "Assurance bientôt expirée";
            String message = "Votre assurance expire dans " + joursRestants
                    + " jour(s). Contrat : " + souscription.getNumeroContrat();

            notificationUserService.createNotification(
                    souscription.getUtilisateur(),
                    titre,
                    message,
                    NotificationType.ASSURANCE_EXPIRATION
            );

            try {
                if (souscription.getUtilisateur().getEmail() != null
                        && !souscription.getUtilisateur().getEmail().isBlank()) {

                    mailService.sendMail(
                            souscription.getUtilisateur().getEmail(),
                            "Votre assurance expire bientôt - CampConnect",
                            "Bonjour " + souscription.getUtilisateur().getNom() + ",\n\n"
                                    + message + "\n\n"
                                    + "Cordialement,\nCampConnect"
                    );
                }
            } catch (Exception e) {
                System.out.println("Erreur mail expiration assurance : " + e.getMessage());
            }

            souscription.setNotificationExpirationEnvoyee(true);
            souscriptionRepository.save(souscription);

            System.out.println("Notification expiration envoyée pour contrat : "
                    + souscription.getNumeroContrat());
        }
    }
}