package com.esprit.campconnect.siteCamping.service;

import com.esprit.campconnect.siteCamping.dto.*;
import com.esprit.campconnect.siteCamping.entity.SiteCamping;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface ISiteCampingService {

    SiteCampingResponse patchSiteCamping(Long idSite, SiteCampingUpdateRequest updatedData);

    SiteCampingResponse getSiteCampingById(Long idSite);

    List<SiteCampingResponse> getAllSiteCampings();

    SiteCampingResponse addSiteCamping(SiteCampingCreateRequest request);

    SiteCampingResponse closeSiteCamping(Long idSite);

    List<SiteCampingResponse> getMySites();

    SiteAvailabilityResponse getAvailability(Long idSite, LocalDate dateDebut, LocalDate dateFin);
}
