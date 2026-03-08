package com.esprit.campconnect.User.Service;


import com.esprit.campconnect.User.Entity.Profil;
import com.esprit.campconnect.User.Repository.ProfilRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfilServiceImp implements IProfilService {
    private final ProfilRepository profilRepository;

    @Override
    public List<Profil> retrieveAllProfils() {
        return profilRepository.findAll();
    }

    @Override
    public Profil retrieveProfil(Long id) {
        return profilRepository.findById(id).orElse(null);
    }

    @Override
    public Profil addProfil(Profil profil) {
        return profilRepository.save(profil);
    }

    @Override
    public Profil updateProfil(Profil profil) {
        return profilRepository.save(profil);
    }

    @Override
    public void removeProfil(Long id) {
        profilRepository.deleteById(id);
    }
}
