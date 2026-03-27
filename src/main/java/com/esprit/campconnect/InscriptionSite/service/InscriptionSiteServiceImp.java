package com.esprit.campconnect.InscriptionSite.service;

import com.esprit.campconnect.InscriptionSite.dto.InscriptionSiteCreateRequest;
import com.esprit.campconnect.InscriptionSite.dto.InscriptionSiteUpdateRequest;
import com.esprit.campconnect.InscriptionSite.entity.InscriptionSite;
import com.esprit.campconnect.InscriptionSite.entity.StatutInscription;
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

    private void updateSiteStatus(SiteCamping site) {
        Integer reservedGuests = inscriptionSiteRepository
                .sumGuestsBySiteAndStatut(site.getIdSite(), StatutInscription.CONFIRMED);

        if (reservedGuests == null) {
            reservedGuests = 0;
        }

        int remainingCapacity = site.getCapacite() - reservedGuests;

        if (site.getStatutDispo() == StatutDispo.CLOSED) {
            return; // not change closed sites
        }

        if (remainingCapacity <= 0) {
            site.setStatutDispo(StatutDispo.FULL);
        } else {
            site.setStatutDispo(StatutDispo.AVAILABLE);
        }

        siteCampingRepository.save(site);
    }

    @Override
    public InscriptionSite addInscriptionSite(InscriptionSiteCreateRequest request) {
        SiteCamping site = siteCampingRepository.findById(request.getSiteId())
                .orElseThrow(() -> new RuntimeException("Site not found"));

        Integer reservedGuests = inscriptionSiteRepository
                .sumGuestsBySiteAndStatut(site.getIdSite(), StatutInscription.CONFIRMED);

        int remainingCapacity = site.getCapacite() - reservedGuests;

        if (!request.getDateFin().isAfter(request.getDateDebut())) {
            throw new RuntimeException("dateFin must be after dateDebut");
        }

        if (request.getNumberOfGuests() == null || request.getNumberOfGuests() <= 0) {
            throw new RuntimeException("numberOfGuests must be greater than 0");
        }

        if (request.getNumberOfGuests() > remainingCapacity) {
            throw new RuntimeException("numberOfGuests exceeds remaining capacity");
        }

        if (site.getStatutDispo() == StatutDispo.FULL || site.getStatutDispo() == StatutDispo.CLOSED) {
            throw new RuntimeException("This site is not available for booking");
        }

        InscriptionSite inscriptionSite = new InscriptionSite();
        inscriptionSite.setDateDebut(request.getDateDebut());
        inscriptionSite.setDateFin(request.getDateFin());
        inscriptionSite.setNumberOfGuests(request.getNumberOfGuests());
        inscriptionSite.setStatut(StatutInscription.PENDING);
        inscriptionSite.setSiteCamping(site);

        InscriptionSite saved = inscriptionSiteRepository.save(inscriptionSite);

        updateSiteStatus(site);

        return saved;
    }

    @Override
    public InscriptionSite patchInscriptionSite(Long idInscription, InscriptionSiteUpdateRequest request) {
        InscriptionSite existing = inscriptionSiteRepository.findById(idInscription)
                .orElseThrow(() -> new IllegalArgumentException(
                        "InscriptionSite not found with id: " + idInscription));

        if (existing.getStatut() != StatutInscription.PENDING) {
            throw new RuntimeException("Only pending inscriptions can be modified");
        }

        if (request.getDateDebut() != null)
            existing.setDateDebut(request.getDateDebut());

        if (request.getDateFin() != null)
            existing.setDateFin(request.getDateFin());

        if (existing.getDateDebut() != null && existing.getDateFin() != null) {
            if (!existing.getDateFin().isAfter(existing.getDateDebut())) {
                throw new RuntimeException("dateFin must be after dateDebut");
            }
        }

        if (request.getNumberOfGuests() != null) {
            if (request.getNumberOfGuests() <= 0) {
                throw new RuntimeException("numberOfGuests must be greater than 0");
            }

            SiteCamping site = existing.getSiteCamping();

            Integer reservedGuests = inscriptionSiteRepository
                    .sumGuestsBySiteAndStatut(site.getIdSite(), StatutInscription.CONFIRMED);

            int remainingCapacity = site.getCapacite() - reservedGuests;

            if (request.getNumberOfGuests() > remainingCapacity) {
                throw new RuntimeException("numberOfGuests exceeds remaining capacity");
            }

            existing.setNumberOfGuests(request.getNumberOfGuests());
        }

        InscriptionSite saved = inscriptionSiteRepository.save(existing);

        return saved;
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

    @Override
    public InscriptionSite confirmInscriptionSite(Long idInscription) {
        InscriptionSite inscription = inscriptionSiteRepository.findById(idInscription)
                .orElseThrow(() -> new IllegalArgumentException(
                        "InscriptionSite not found with id: " + idInscription));

        if (inscription.getStatut() != StatutInscription.PENDING) {
            throw new RuntimeException("Only pending inscriptions can be confirmed");
        }

        SiteCamping site = inscription.getSiteCamping();

        if (site.getStatutDispo() == StatutDispo.CLOSED) {
            throw new RuntimeException("This site is closed");
        }

        Integer confirmedGuests = inscriptionSiteRepository
                .sumGuestsBySiteAndStatut(site.getIdSite(), StatutInscription.CONFIRMED);

        int remainingCapacity = site.getCapacite() - confirmedGuests;

        if (inscription.getNumberOfGuests() > remainingCapacity) {
            throw new RuntimeException("Not enough remaining capacity to confirm this inscription");
        }

        inscription.setStatut(StatutInscription.CONFIRMED);
        InscriptionSite saved = inscriptionSiteRepository.save(inscription);

        updateSiteStatus(site);

        return saved;
    }

    @Override
    public InscriptionSite cancelInscriptionSite(Long idInscription) {
        InscriptionSite inscription = inscriptionSiteRepository.findById(idInscription)
                .orElseThrow(() -> new IllegalArgumentException(
                        "InscriptionSite not found with id: " + idInscription));

        if (inscription.getStatut() == StatutInscription.CANCELLED) {
            throw new RuntimeException("Inscription is already cancelled");
        }

        inscription.setStatut(StatutInscription.CANCELLED);
        InscriptionSite saved = inscriptionSiteRepository.save(inscription);

        updateSiteStatus(inscription.getSiteCamping());

        return saved;
    }
}
