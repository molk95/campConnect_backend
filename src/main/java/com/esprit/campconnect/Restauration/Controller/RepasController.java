package com.esprit.campconnect.Restauration.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import com.esprit.campconnect.Restauration.Entity.Repas;
import com.esprit.campconnect.Restauration.Service.RepasService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/repas")
@CrossOrigin("*")
@RequiredArgsConstructor
public class RepasController {

    private final RepasService repasService;
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Repas create(
            @RequestParam("nom") String nom,
            @RequestParam("prix") Double prix,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        return repasService.createRepas(nom, prix, image);
    }

    @GetMapping
    public List<Repas> getAll() {
        return repasService.getAllRepas();
    }

    @GetMapping("/{id}")
    public Repas getById(@PathVariable Long id) {
        return repasService.getRepasById(id);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Repas update(
            @PathVariable Long id,
            @RequestParam("nom") String nom,
            @RequestParam("prix") Double prix,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        return repasService.updateRepas(id, nom, prix, image);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repasService.deleteRepas(id);
    }
}