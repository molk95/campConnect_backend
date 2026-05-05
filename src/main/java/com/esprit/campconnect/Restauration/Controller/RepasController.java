package com.esprit.campconnect.Restauration.Controller;
import com.esprit.campconnect.Restauration.DTO.RepasRequestDTO;
import com.esprit.campconnect.Restauration.DTO.RepasResponseDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import com.esprit.campconnect.Restauration.Entity.Repas;
import com.esprit.campconnect.Restauration.Service.RepasService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repas")
@CrossOrigin("*")
@RequiredArgsConstructor
public class RepasController {

    private final RepasService repasService;

    @PostMapping
    @PreAuthorize("hasAuthority('GERANT_RESTAU')")
    public RepasResponseDTO create(@RequestBody RepasRequestDTO request) {
        return repasService.createRepas(request);
    }

   @GetMapping
   public List<RepasResponseDTO> getAll() {
        return repasService.getAllRepas();
    }


    @GetMapping("/{id}")
    public RepasResponseDTO getById(@PathVariable Long id) {
        return repasService.getRepasById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERANT_RESTAU')")
    public RepasResponseDTO update(@PathVariable Long id,
                                   @RequestBody RepasRequestDTO request) {
        return repasService.updateRepas(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERANT_RESTAU')")
    public void delete(@PathVariable Long id) {
        repasService.deleteRepas(id);
    }
}