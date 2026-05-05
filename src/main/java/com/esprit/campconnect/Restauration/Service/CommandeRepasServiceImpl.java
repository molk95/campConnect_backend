package com.esprit.campconnect.Restauration.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.esprit.campconnect.Restauration.DTO.CommandeRequestDTO;
import com.esprit.campconnect.Restauration.DTO.LigneCommandeDTO;
import com.esprit.campconnect.Restauration.Entity.CommandeRepas;
import com.esprit.campconnect.Restauration.Entity.LigneCommandeRepas;
import com.esprit.campconnect.Restauration.Entity.Repas;
import com.esprit.campconnect.Restauration.Enum.StatutCommandeRepas;
import com.esprit.campconnect.Restauration.Repository.CommandeRepasRepository;
import com.esprit.campconnect.Restauration.Repository.RepasRepository;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service("commandeRepasService")
@RequiredArgsConstructor
public class CommandeRepasServiceImpl implements CommandeRepasService {

    private final CommandeRepasRepository commandeRepo;
    private final UtilisateurRepository userRepo;
    private final RepasRepository repasRepo;
    private final UtilisateurRepository utilisateurRepository;

    //GetByCurrentUser
    private Utilisateur getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }

    @Override
    public CommandeRepas createCommande(CommandeRequestDTO request) {

        // VALIDATION
        if (request.getLignes() == null || request.getLignes().isEmpty()) {
            throw new RuntimeException("Commande vide");
        }

        // UTILISATEUR CONNECTÉ (réutilisation de ta méthode)
        Utilisateur user = getCurrentUser();

        // CRÉATION COMMANDE
        CommandeRepas cmd = new CommandeRepas();
        cmd.setDateCommande(LocalDate.now());
        cmd.setUtilisateur(user);
        cmd.setStatut(StatutCommandeRepas.EN_ATTENTE);

        double total = 0;

        for (LigneCommandeDTO dto : request.getLignes()) {

            // VALIDATION quantité
            if (dto.getQuantite() <= 0) {
                throw new RuntimeException("Quantité invalide");
            }

            // RÉCUPÉRER REPAS
            Repas repas = repasRepo.findById(dto.getRepasId())
                    .orElseThrow(() -> new RuntimeException("Repas non trouvé avec id: " + dto.getRepasId()));

            // CRÉER LIGNE
            LigneCommandeRepas ligne = new LigneCommandeRepas();
            ligne.setRepas(repas);
            ligne.setQuantite(dto.getQuantite());
            ligne.setPrixUnitaire(repas.getPrix());
            ligne.setCommandeRepas(cmd); // ⚠️ IMPORTANT

            // si tu as une méthode helper
            // cmd.addLigne(ligne);
            // sinon :
            cmd.getLignes().add(ligne);

            total += dto.getQuantite() * repas.getPrix();
        }

        cmd.setMontantTotal(total);

        return commandeRepo.save(cmd);
    }



    @Override
    public List<CommandeRepas> getAll() {
        return commandeRepo.findAll();
    }

    @Override
    public CommandeRepas getById(Long id) {
        return commandeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
    }

    @Override
    public CommandeRepas updateStatus(Long id, StatutCommandeRepas statut) {

        CommandeRepas cmd = getById(id);

        if (cmd.getStatut() == StatutCommandeRepas.LIVREE) {
            throw new RuntimeException("Commande déjà livrée");
        }

        cmd.setStatut(statut);

        return commandeRepo.save(cmd);
    }

    @Override
    public void delete(Long id) {

        CommandeRepas cmd = getById(id);

        if (cmd.getStatut() == StatutCommandeRepas.LIVREE) {
            throw new RuntimeException("Impossible de supprimer une commande livrée");
        }

        commandeRepo.delete(cmd);
    }
}