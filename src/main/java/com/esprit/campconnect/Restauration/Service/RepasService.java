package com.esprit.campconnect.Restauration.Service;

import com.esprit.campconnect.Restauration.DTO.RepasRequestDTO;
import com.esprit.campconnect.Restauration.DTO.RepasResponseDTO;
import com.esprit.campconnect.Restauration.Entity.Repas;
import java.util.List;

public interface RepasService {


    //GetByCurrentUser
    RepasResponseDTO createRepas(RepasRequestDTO request);
    List<RepasResponseDTO> getMyRepas();

    List<RepasResponseDTO> getAllRepas();

    RepasResponseDTO  getRepasById(Long id);


    RepasResponseDTO updateRepas(Long id, RepasRequestDTO request);

    void deleteRepas(Long id);
}