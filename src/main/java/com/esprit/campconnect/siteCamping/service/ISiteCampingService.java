package com.esprit.campconnect.siteCamping.service;

import com.esprit.campconnect.siteCamping.dto.SiteCampingCreateRequest;
import com.esprit.campconnect.siteCamping.dto.SiteCampingResponse;
import com.esprit.campconnect.siteCamping.dto.SiteCampingUpdateRequest;
import com.esprit.campconnect.siteCamping.entity.SiteCamping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISiteCampingService {

    SiteCamping patchSiteCamping(Long idSite, SiteCampingUpdateRequest updatedData);

    SiteCampingResponse getSiteCampingById(Long idSite);

    List<SiteCampingResponse> getAllSiteCampings();

    void deleteSiteCamping(Long idSite);

    SiteCamping addSiteCamping(SiteCampingCreateRequest request);
    SiteCamping closeSiteCamping(Long idSite);
}
