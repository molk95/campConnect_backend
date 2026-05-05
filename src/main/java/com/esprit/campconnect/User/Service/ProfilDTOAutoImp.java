package com.esprit.campconnect.User.Service;

import com.esprit.campconnect.User.DTO.ProfilDTO;
import com.esprit.campconnect.User.Entity.Profil;
import com.esprit.campconnect.User.Repository.ProfilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.esprit.campconnect.User.Service.IProfilDTOAuto;
import java.util.List;

@Service
@RequiredArgsConstructor
@Component
public class ProfilDTOAutoImp {
    private final ProfilRepository profilRepository;
    private final IProfilDTOAuto iProfilDTOAuto = org.mapstruct.factory.Mappers.getMapper(IProfilDTOAuto.class);

    public ProfilDTO getProfil(Long id) {
        Profil profil = profilRepository.findById(id).orElse(null);
        return iProfilDTOAuto.DTOtoDTO(profil);
    }

    public List<ProfilDTO> getAllProfils() {
        return profilRepository.findAll()
                .stream()
                .map(iProfilDTOAuto::DTOtoDTO)
                .toList();
    }
}
