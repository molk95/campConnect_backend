package com.esprit.campconnect.Restauration.Service;

import com.esprit.campconnect.Restauration.Entity.Repas;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface RepasService {
    Repas createRepas(String nom, Double prix, MultipartFile image);

    List<Repas> getAllRepas();

    Repas getRepasById(Long id);

    Repas updateRepas(Long id, String nom, Double prix, MultipartFile image);

    void deleteRepas(Long id);
}