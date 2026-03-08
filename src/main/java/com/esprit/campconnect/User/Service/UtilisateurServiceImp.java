package com.esprit.campconnect.User.Service;


import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UtilisateurServiceImp implements IUtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<Utilisateur> retrieveAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @Override
    public Utilisateur retrieveUtilisateur(Long id) {
        return utilisateurRepository.findById(id).orElse(null);
    }

    @Override
    public Utilisateur addUtilisateur(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public Utilisateur updateUtilisateur(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public void removeUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }


}
