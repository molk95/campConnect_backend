package com.esprit.campconnect.SiteCampingAvis.service;

import com.esprit.campconnect.SiteCampingAvis.dto.*;
import com.esprit.campconnect.SiteCampingAvis.entity.SiteCampingAvis;

import java.util.List;

public interface ISiteCampingAvisService {
    SiteCampingAvisResponse createSiteCampingAvis(Long siteId, SiteCampingAvisCreateRequest request);
    SiteCampingAvisResponse patchSiteCampingAvis(Long idAvis, SiteCampingAvisUpdateRequest request);
    void deleteSiteCampingAvis(Long idAvis);
    List<SiteCampingAvisResponse> getAvisBySite(Long siteId);
    List<SiteCampingAvisAdminResponse> getAllAvisForAdmin();
    List<SiteCampingAvisAdminResponse> getMyCampAvis();
    SiteCampingRatingResponse getAverageRatingBySite(Long siteId);
}
