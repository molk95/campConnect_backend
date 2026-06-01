package com.esprit.campconnect.Restauration.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.esprit.campconnect.Restauration.Service.RepasCloudinaryService;
import com.esprit.campconnect.Restauration.Entity.Repas;
import com.esprit.campconnect.Restauration.Repository.RepasRepository;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.List;
@Service
@RequiredArgsConstructor
public class RepasServiceImpl implements RepasService {

    private final RepasRepository repasRepository;
    private final UtilisateurRepository utilisateurRepository;

    private final RepasCloudinaryService cloudinaryService; // 👈 interface commune

          @Override
          public Repas createRepas(String nom, Double prix, MultipartFile image) {

            if (nom == null || nom.isEmpty()) {
                throw new RuntimeException("Nom obligatoire");
            }
            if (prix == null || prix < 0) {
                throw new RuntimeException("Prix invalide");
            }
            if (image == null || image.isEmpty()) {
                throw new RuntimeException("Image obligatoire");
            }

            // 👇 upload vers Cloudinary — même méthode que ta collègue
            Map<String, String> uploadResult = cloudinaryService.uploadImage(image);

            // 👇 récupérer utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Repas repas = new Repas();
            repas.setNom(nom);
            repas.setPrix(prix);
            repas.setImage(uploadResult.get("imageUrl"));           // 👈 URL complète
            repas.setImagePublicId(uploadResult.get("imagePublicId")); // 👈 pour suppression
            repas.setUtilisateur(utilisateur);

            return repasRepository.save(repas);
        }

        @Override
        public List<Repas> getAllRepas() {
            return repasRepository.findAll();
        }

        @Override
        public Repas getRepasById(Long id) {
            return repasRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Repas non trouvé"));
        }

        @Override
        public Repas updateRepas(Long id, String nom, Double prix, MultipartFile image) {

            Repas repas = repasRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Repas non trouvé"));

            repas.setNom(nom);
            repas.setPrix(prix);

            if (image != null && !image.isEmpty()) {
                // 👇 supprimer ancienne image sur Cloudinary via publicId
                if (repas.getImagePublicId() != null) {
                    cloudinaryService.deleteImage(repas.getImagePublicId());
                }
                // 👇 uploader la nouvelle
                Map<String, String> uploadResult = cloudinaryService.uploadImage(image);
                repas.setImage(uploadResult.get("imageUrl"));
                repas.setImagePublicId(uploadResult.get("imagePublicId"));
            }

            return repasRepository.save(repas);
        }

        @Override
        public void deleteRepas(Long id) {
            Repas repas = repasRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Repas non trouvé"));

            // 👇 supprimer image sur Cloudinary avant suppression BDD
            if (repas.getImagePublicId() != null) {
                cloudinaryService.deleteImage(repas.getImagePublicId());
            }

            repasRepository.deleteById(id);
        }
    }