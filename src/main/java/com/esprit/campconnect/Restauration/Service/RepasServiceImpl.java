package com.esprit.campconnect.Restauration.Service;

import com.esprit.campconnect.Restauration.Entity.Repas;
import com.esprit.campconnect.Restauration.Repository.RepasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class RepasServiceImpl implements RepasService {

    private final RepasRepository repasRepository;

    @Override
    public Repas createRepas(Repas repas) {
        // VALIDATION NOM
        if (repas.getNom() == null || repas.getNom().isEmpty()) {
            throw new RuntimeException("Nom obligatoire");
        }
        if (repas.getPrix() < 0) {
            throw new RuntimeException("Prix invalide");
        }
        return repasRepository.save(repas);
    }

    @Override
    public List<Repas> getAllRepas() {
        return repasRepository.findAll();
    }

    @Override
    public Repas getRepasById(Long id) {
        return repasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repas non trouvé"));
    }

    @Override
    public Repas updateRepas(Long id, Repas repas) {

        if (repas.getNom() == null || repas.getNom().isEmpty()) {
            throw new RuntimeException("Nom obligatoire");
        }

        if (repas.getPrix() < 0) {
            throw new RuntimeException("Prix invalide");
        }

        Repas existing = getRepasById(id);

        existing.setNom(repas.getNom());
        existing.setPrix(repas.getPrix());

        return repasRepository.save(existing);
    }

    @Override
    public void deleteRepas(Long id) {
        repasRepository.deleteById(id);
    }
}