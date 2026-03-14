package com.esprit.campconnect.InscriptionSite.service;

import com.esprit.campconnect.InscriptionSite.entity.InscriptionSite;
import com.esprit.campconnect.InscriptionSite.repository.InscriptionSiteRepository;
import com.esprit.campconnect.siteCamping.entity.SiteCamping;
import com.esprit.campconnect.siteCamping.entity.StatutDispo;
import com.esprit.campconnect.siteCamping.repository.SiteCampingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class InscriptionSiteServiceImp implements IInscriptionSiteService{
    private final InscriptionSiteRepository inscriptionSiteRepository;
    private final SiteCampingRepository siteCampingRepository;

    @Override
    public InscriptionSite addInscriptionSite(InscriptionSite inscriptionSite) {
        SiteCamping site = siteCampingRepository.findById(inscriptionSite.getSiteCamping().getIdSite())
                .orElseThrow(() -> new RuntimeException("Site not found"));

        if (!inscriptionSite.getDateFin().isAfter(inscriptionSite.getDateDebut())) {
            throw new RuntimeException("dateFin must be after dateDebut");
        }

        if (inscriptionSite.getNumberOfGuests() <= 0) {
            throw new RuntimeException("numberOfGuests must be greater than 0");
        }

        if (inscriptionSite.getNumberOfGuests() > site.getCapacite()) {
            throw new RuntimeException("numberOfGuests exceeds site capacity");
        }

        if (site.getStatutDispo() == StatutDispo.FULL || site.getStatutDispo() == StatutDispo.CLOSED) {
            throw new RuntimeException("This site is not available for booking");
        }

        inscriptionSite.setSiteCamping(site);
        return inscriptionSiteRepository.save(inscriptionSite);
    }

    @Override
    public InscriptionSite patchInscriptionSite(Long idInscription, InscriptionSite updatedData) {
        InscriptionSite existing = inscriptionSiteRepository.findById(idInscription)
                .orElseThrow(() -> new IllegalArgumentException(
                        "InscriptionSite not found with id: " + idInscription));

        if (updatedData.getDateDebut() != null)
            existing.setDateDebut(updatedData.getDateDebut());

        if (updatedData.getDateFin() != null)
            existing.setDateFin(updatedData.getDateFin());

        if (updatedData.getStatut() != null)
            existing.setStatut(updatedData.getStatut());

        if (updatedData.getSiteCamping() != null)
            existing.setSiteCamping(updatedData.getSiteCamping());

        return inscriptionSiteRepository.save(existing);
    }


    @Override
    public InscriptionSite getInscriptionSiteById(Long idInscription) {
        return inscriptionSiteRepository.findById(idInscription)
                .orElseThrow(() -> new IllegalArgumentException(
                        "InscriptionSite not found with id: " + idInscription
                ));
    }

    @Override
    public List<InscriptionSite> getAllInscriptionSites() {
        return inscriptionSiteRepository.findAll();
    }

    @Override
    public void deleteInscriptionSite(Long idInscription) {
        if (!inscriptionSiteRepository.existsById(idInscription)) {
            throw new IllegalArgumentException("InscriptionSite not found with id: " + idInscription);
        }
        inscriptionSiteRepository.deleteById(idInscription);
    }

    @Override
    public List<InscriptionSite> getBySiteCamping(Long idSite) {
        return inscriptionSiteRepository.findBySiteCamping_IdSite(idSite);
    }
}
