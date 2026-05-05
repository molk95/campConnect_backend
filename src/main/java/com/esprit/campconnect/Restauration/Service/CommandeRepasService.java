package com.esprit.campconnect.Restauration.Service;

import com.esprit.campconnect.Restauration.DTO.CommandeRequestDTO;
import com.esprit.campconnect.Restauration.Entity.CommandeRepas;
import com.esprit.campconnect.Restauration.Enum.StatutCommandeRepas;

import java.util.List;

public interface CommandeRepasService {

    CommandeRepas createCommande(CommandeRequestDTO request);

    List<CommandeRepas> getAll();

    CommandeRepas getById(Long id);

    CommandeRepas updateStatus(Long id, StatutCommandeRepas statut);

    void delete(Long id);
}