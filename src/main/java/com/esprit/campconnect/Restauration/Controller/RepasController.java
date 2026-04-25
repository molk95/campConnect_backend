package com.esprit.campconnect.Restauration.Controller;
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
    @PreAuthorize("hasRole('GERANT_RESTAU')")
    public Repas create(@RequestBody Repas repas) {
        return repasService.createRepas(repas);
    }
   @GetMapping
   public List<Repas> getAll() {
        return repasService.getAllRepas();
    }


    @GetMapping("/{id}")
    public Repas getById(@PathVariable Long id) {
        return repasService.getRepasById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERANT_RESTAU')")
    public Repas update(@PathVariable Long id, @RequestBody Repas repas) {
        return repasService.updateRepas(id, repas);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERANT_RESTAU')")
    public void delete(@PathVariable Long id) {
        repasService.deleteRepas(id);
    }
}