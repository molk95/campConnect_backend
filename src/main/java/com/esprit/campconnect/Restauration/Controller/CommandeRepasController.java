package com.esprit.campconnect.Restauration.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import com.esprit.campconnect.Restauration.DTO.CommandeRequestDTO;
import com.esprit.campconnect.Restauration.Entity.CommandeRepas;
import com.esprit.campconnect.Restauration.Enum.StatutCommandeRepas;
import com.esprit.campconnect.Restauration.Service.CommandeRepasService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commandes-repas")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CommandeRepasController {

    private final CommandeRepasService commandeService;


    @PostMapping
    public CommandeRepas create(@RequestBody CommandeRequestDTO request) {
        return commandeService.createCommande(request);
    }

    @GetMapping
    public List<CommandeRepas> getAll() {
        return commandeService.getAll();
    }

    @GetMapping("/{id}")
    public CommandeRepas getById(@PathVariable Long id) {
        return commandeService.getById(id);
    }

    @PutMapping("/{id}/statut")
    public CommandeRepas updateStatus(
            @PathVariable Long id,
            @RequestParam StatutCommandeRepas statut) {
        return commandeService.updateStatus(id, statut);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        commandeService.delete(id);
    }
}