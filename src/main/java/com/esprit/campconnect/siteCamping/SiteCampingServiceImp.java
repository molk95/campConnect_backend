package com.esprit.campconnect.siteCamping;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SiteCampingServiceImp implements ISiteCampingService {
    private final SiteCampingRepository siteCampingRepository;


    @Override
    public SiteCamping addSiteCamping(SiteCamping siteCamping) {
        return siteCampingRepository.save(siteCamping);
    }

    @Override
    public SiteCamping patchSiteCamping(Long idSite, SiteCamping updatedData) {
        SiteCamping existing = siteCampingRepository.findById(idSite)
                .orElseThrow(() -> new IllegalArgumentException(
                        "SiteCamping not found with id: " + idSite));

        if (updatedData.getNom() != null)
            existing.setNom(updatedData.getNom());

        if (updatedData.getLocalisation() != null)
            existing.setLocalisation(updatedData.getLocalisation());

        if (updatedData.getCapacite() != 0)
            existing.setCapacite(updatedData.getCapacite());

        if (updatedData.getPrixParNuit() != 0)
            existing.setPrixParNuit(updatedData.getPrixParNuit());

        if (updatedData.getStatutDispo() != null)
            existing.setStatutDispo(updatedData.getStatutDispo());

        return siteCampingRepository.save(existing);
    }

    @Override
    public SiteCamping getSiteCampingById(Long idSite) {
        return siteCampingRepository.findById(idSite)
                .orElseThrow(() -> new IllegalArgumentException("SiteCamping not found with id: " + idSite));
    }

    @Override
    public List<SiteCamping> getAllSiteCampings() {
        return siteCampingRepository.findAll();
    }

    @Override
    public void deleteSiteCamping(Long idSite) {
        if (!siteCampingRepository.existsById(idSite)) {
            throw new IllegalArgumentException("SiteCamping not found with id: " + idSite);
        }
        siteCampingRepository.deleteById(idSite);
    }
}
