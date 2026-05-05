package com.esprit.campconnect.Restauration.Service;

import com.esprit.campconnect.Restauration.DTO.RepasResponseDTO;
import com.esprit.campconnect.Restauration.Entity.Repas;
import com.esprit.campconnect.Restauration.Repository.RepasRepository;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import com.esprit.campconnect.Restauration.DTO.RepasRequestDTO;
import java.util.List;
@Service
@RequiredArgsConstructor

public class RepasServiceImpl implements RepasService {

    private final RepasRepository repasRepository;

    private final UtilisateurRepository utilisateurRepository;
    //GetByCurrentUser

    private Utilisateur getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }

    //GetByCurrentUser
    @Override
    public RepasResponseDTO createRepas(RepasRequestDTO request) {

        if (request.getNom() == null || request.getNom().isEmpty()) {
            throw new RuntimeException("Nom obligatoire");
        }

        if (request.getPrix() < 0) {
            throw new RuntimeException("Prix invalide");
        }

        Utilisateur currentUser = getCurrentUser();

        Repas repas = new Repas();
        repas.setNom(request.getNom());
        repas.setPrix(request.getPrix());
        repas.setImage(request.getImage());
        repas.setUtilisateur(currentUser); // 🔥 important

        return mapToResponse(repasRepository.save(repas));
    }

    //GetByCurrentUser
    public List<RepasResponseDTO> getMyRepas() {
        Utilisateur currentUser = getCurrentUser();

        return repasRepository.findByUtilisateurId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public List<RepasResponseDTO> getAllRepas() {
        return repasRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    @Override
    public RepasResponseDTO getRepasById(Long id) {

        Repas repas = repasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repas non trouvé"));

        return mapToResponse(repas);
    }

    private Repas getRepasEntityById(Long id) {
        return repasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repas non trouvé"));
    }
    @Override
    public RepasResponseDTO updateRepas(Long id, RepasRequestDTO request) {

        Utilisateur currentUser = getCurrentUser();
        Repas existing = getRepasEntityById(id);

        if (!existing.getUtilisateur().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Non autorisé");
        }

        existing.setNom(request.getNom());
        existing.setPrix(request.getPrix());

        return mapToResponse(repasRepository.save(existing));
    }
    @Override
    public void deleteRepas(Long id) {

        Utilisateur currentUser = getCurrentUser();
        Repas existing = getRepasEntityById(id);

        if (!existing.getUtilisateur().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Non autorisé");
        }

        repasRepository.delete(existing);
    }

    private RepasResponseDTO mapToResponse(Repas repas) {
        RepasResponseDTO dto = new RepasResponseDTO();
        dto.setId(repas.getId());
        dto.setNom(repas.getNom());
        dto.setPrix(repas.getPrix());
        dto.setImage(repas.getImage());
        if (repas.getUtilisateur() != null) {
            dto.setUtilisateurId(repas.getUtilisateur().getId());
            dto.setUtilisateurEmail(repas.getUtilisateur().getEmail());
        }

        return dto;
    }




}