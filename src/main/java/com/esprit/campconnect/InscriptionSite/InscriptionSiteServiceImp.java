package com.esprit.campconnect.InscriptionSite;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class InscriptionSiteServiceImp implements IInscriptionSiteService{
    private final InscriptionSiteRepository inscriptionSiteRepository;
    @Override
    public InscriptionSite addInscriptionSite(InscriptionSite inscriptionSite) {
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
