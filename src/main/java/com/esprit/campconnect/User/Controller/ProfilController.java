package com.esprit.campconnect.User.Controller;

import com.esprit.campconnect.User.DTO.ProfilDTO;
import com.esprit.campconnect.User.Entity.Profil;
import com.esprit.campconnect.User.Repository.ProfilRepository;
import com.esprit.campconnect.User.Service.IProfilService;
import com.esprit.campconnect.User.Service.ProfilDTOAutoImp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="Gestion de profil")
@RestController
@RequiredArgsConstructor
@RequestMapping("/profil")
@CrossOrigin("*")


public class ProfilController {
    private final IProfilService profilService;
    private final ProfilDTOAutoImp profilDTOAutoImp;

    @Operation(description = "Récupérer un profil sans données sensibles")
    @GetMapping("/DtoProfile/{id}")
    public ProfilDTO getProfilDto(@PathVariable Long id) {
        return profilDTOAutoImp.getProfil(id);
    }

    @Operation(description = "Récupérer tous les profils")
    @GetMapping("/allDtoProfile")
    public List<ProfilDTO> getAllProfilsDto() {
        return profilDTOAutoImp.getAllProfils();
    }

    @GetMapping("/getAllProfils")
    public List<Profil> getAllProfils() {
        return profilService.retrieveAllProfils();
    }

    @GetMapping("/getProfil/{id}")
    public Profil getProfil(@PathVariable Long id) {
        return profilService.retrieveProfil(id);
    }


    @PutMapping("/updateProfil")
    public Profil modifyProfil(@RequestBody Profil profil) {
        return profilService.updateProfil(profil);
    }


}
