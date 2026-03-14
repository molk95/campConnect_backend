package com.esprit.campconnect.User.Service;

import com.esprit.campconnect.User.Entity.Profil;

import java.util.List;

public interface IProfilService {
    List<Profil> retrieveAllProfils();
    Profil retrieveProfil(Long id);
    Profil updateProfil(Profil profil);
}
