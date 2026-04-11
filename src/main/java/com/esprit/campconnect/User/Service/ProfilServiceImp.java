package com.esprit.campconnect.User.Service;

import com.esprit.campconnect.Notification.Entity.NotificationType;
import com.esprit.campconnect.Notification.Entity.NotificationUser;
import com.esprit.campconnect.Notification.Service.INotificationUserService;
import com.esprit.campconnect.User.Entity.Profil;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.ProfilRepository;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import com.esprit.campconnect.common.CloudinaryServiceImp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfilServiceImp implements IProfilService {

    private final ProfilRepository profilRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CloudinaryServiceImp cloudinaryServiceImp;
    private final INotificationUserService notificationUserService;

    @Override
    public List<Profil> retrieveAllProfils() {
        return profilRepository.findAll();
    }

    @Override
    public Profil retrieveProfil(Long id) {
        return profilRepository.findById(id).orElse(null);
    }

    @Override
    public Profil updateProfil(Profil profil) {
        return profilRepository.save(profil);
    }

    @Override
    public Profil getProfileByUserEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (utilisateur.getProfil() == null) {
            Profil profil = new Profil();
            utilisateur.setProfil(profil);
            profil.setUtilisateur(utilisateur);
            utilisateurRepository.save(utilisateur);
        }

        return utilisateur.getProfil();
    }

    @Override
    public Profil updateMyProfile(String email, Profil profilData) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Profil profil = utilisateur.getProfil();
        if (profil == null) {
            profil = new Profil();
            profil.setUtilisateur(utilisateur);
        }

        profil.setAdresse(profilData.getAdresse());
        profil.setBiographie(profilData.getBiographie());

        if (profilData.getPhoto() != null && !profilData.getPhoto().isBlank()) {
            profil.setPhoto(profilData.getPhoto());
        }

        Profil savedProfil = profilRepository.save(profil);
        utilisateur.setProfil(savedProfil);
        utilisateurRepository.save(utilisateur);

        notificationUserService.createNotification(
                utilisateur,
                "Profil mis à jour",
                "Vos informations de profil ont été mises à jour avec succès.",
                NotificationType.PROFILE_UPDATED
        );

        return savedProfil;
    }

    @Override
    public Profil uploadMyProfileImage(String email, MultipartFile file) {
        try {
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Profil profil = utilisateur.getProfil();
            if (profil == null) {
                profil = new Profil();
                profil.setUtilisateur(utilisateur);
            }

            Map<String, String> uploadResult = cloudinaryServiceImp.uploadImage(file);
            String imageUrl = uploadResult.get("imageUrl");

            profil.setPhoto(imageUrl);

            Profil savedProfil = profilRepository.save(profil);
            utilisateur.setProfil(savedProfil);
            utilisateurRepository.save(utilisateur);

            notificationUserService.createNotification(
                    utilisateur,
                    "Photo de profil mise à jour",
                    "Votre image de profil a été modifiée avec succès.",
                    NotificationType.PROFILE_UPDATED
            );

            return savedProfil;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur upload profil : " + e.getMessage(), e);
        }
    }

    @Override
    public Profil updatePhotoUrl(String email, String photoUrl) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Profil profil = utilisateur.getProfil();
        if (profil == null) {
            profil = new Profil();
            profil.setUtilisateur(utilisateur);
        }

        profil.setPhoto(photoUrl);

        Profil savedProfil = profilRepository.save(profil);
        utilisateur.setProfil(savedProfil);
        utilisateurRepository.save(utilisateur);

        notificationUserService.createNotification(
                utilisateur,
                "Photo de profil mise à jour",
                "L’URL de votre photo de profil a été mise à jour avec succès.",
                NotificationType.PROFILE_UPDATED
        );

        return savedProfil;
    }
}