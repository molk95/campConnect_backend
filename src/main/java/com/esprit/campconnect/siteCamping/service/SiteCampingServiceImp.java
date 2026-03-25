package com.esprit.campconnect.siteCamping.service;

import com.esprit.campconnect.InscriptionSite.entity.StatutInscription;
import com.esprit.campconnect.InscriptionSite.repository.InscriptionSiteRepository;
import com.esprit.campconnect.common.ICloudinaryService;
import com.esprit.campconnect.siteCamping.dto.SiteCampingCreateRequest;
import com.esprit.campconnect.siteCamping.dto.SiteCampingResponse;
import com.esprit.campconnect.siteCamping.dto.SiteCampingUpdateRequest;
import com.esprit.campconnect.siteCamping.entity.SiteCamping;
import com.esprit.campconnect.siteCamping.repository.SiteCampingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SiteCampingServiceImp implements ISiteCampingService {
    private final SiteCampingRepository siteCampingRepository;
    private final ICloudinaryService cloudinaryService;
    private final InscriptionSiteRepository inscriptionSiteRepository;

    @Override
    public SiteCamping patchSiteCamping(Long idSite, SiteCampingUpdateRequest updatedData) {
        SiteCamping existing = siteCampingRepository.findById(idSite)
                .orElseThrow(() -> new IllegalArgumentException(
                        "SiteCamping not found with id: " + idSite));

        if (updatedData.getNom() != null)
            existing.setNom(updatedData.getNom());

        if (updatedData.getLocalisation() != null)
            existing.setLocalisation(updatedData.getLocalisation());

        if (updatedData.getCapacite() != null)
            existing.setCapacite(updatedData.getCapacite());

        if (updatedData.getPrixParNuit() != null)
            existing.setPrixParNuit(updatedData.getPrixParNuit());

        if (updatedData.getDescription() != null)
            existing.setDescription(updatedData.getDescription());

        if (updatedData.getStatutDispo() != null)
            existing.setStatutDispo(updatedData.getStatutDispo());

        if (updatedData.getImage() != null && !updatedData.getImage().isEmpty()) {
            if (existing.getImagePublicId() != null && !existing.getImagePublicId().isBlank()) {
                cloudinaryService.deleteImage(existing.getImagePublicId());
            }

            Map<String, String> uploadResult = cloudinaryService.uploadImage(updatedData.getImage());
            existing.setImageUrl(uploadResult.get("imageUrl"));
            existing.setImagePublicId(uploadResult.get("imagePublicId"));
        }

        return siteCampingRepository.save(existing);
    }

    @Override
    public SiteCampingResponse getSiteCampingById(Long idSite) {
        SiteCamping site = siteCampingRepository.findById(idSite)
                .orElseThrow(() -> new RuntimeException("SiteCamping not found with id: " + idSite));

        return mapToResponse(site);
    }

    @Override
    public List<SiteCampingResponse> getAllSiteCampings() {
        return siteCampingRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private int calculateRemainingCapacity(SiteCamping site) {
        Integer confirmedGuests = inscriptionSiteRepository
                .sumGuestsBySiteAndStatut(site.getIdSite(), StatutInscription.CONFIRMED);

        return site.getCapacite() - confirmedGuests;
    }

    private SiteCampingResponse mapToResponse(SiteCamping site) {
        SiteCampingResponse response = new SiteCampingResponse();
        response.setIdSite(site.getIdSite());
        response.setNom(site.getNom());
        response.setLocalisation(site.getLocalisation());
        response.setCapacite(site.getCapacite());
        response.setRemainingCapacity(calculateRemainingCapacity(site));
        response.setPrixParNuit(site.getPrixParNuit());
        response.setDescription(site.getDescription());
        response.setImageUrl(site.getImageUrl());
        response.setImagePublicId(site.getImagePublicId());
        response.setStatutDispo(site.getStatutDispo());

        return response;
    }

    @Override
    public void deleteSiteCamping(Long idSite) {
        SiteCamping existing = siteCampingRepository.findById(idSite)
                .orElseThrow(() -> new IllegalArgumentException(
                        "SiteCamping not found with id: " + idSite));

        if (existing.getImagePublicId() != null && !existing.getImagePublicId().isBlank()) {
            cloudinaryService.deleteImage(existing.getImagePublicId());
        }

        siteCampingRepository.delete(existing);
    }

    @Override
    public SiteCamping addSiteCamping(SiteCampingCreateRequest request) {
        Map<String, String> uploadResult = cloudinaryService.uploadImage(request.getImage());

        SiteCamping siteCamping = new SiteCamping();
        siteCamping.setNom(request.getNom());
        siteCamping.setLocalisation(request.getLocalisation());
        siteCamping.setCapacite(request.getCapacite());
        siteCamping.setPrixParNuit(request.getPrixParNuit());
        siteCamping.setDescription(request.getDescription());
        siteCamping.setStatutDispo(request.getStatutDispo());

        siteCamping.setImageUrl(uploadResult.get("imageUrl"));
        siteCamping.setImagePublicId(uploadResult.get("imagePublicId"));

        return siteCampingRepository.save(siteCamping);
    }
}
