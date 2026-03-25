package com.esprit.campconnect.SiteCampingAvis.service;

import com.esprit.campconnect.SiteCampingAvis.entity.SiteCampingAvis;

import java.util.List;

public interface ISiteCampingAvisService {
    SiteCampingAvis createSiteCampingAvis(Long siteId, SiteCampingAvis avis);

    SiteCampingAvis patchSiteCampingAvis(Long idAvis, SiteCampingAvis updateData);

    void deleteSiteCampingAvis(Long idAvis);
    List<SiteCampingAvis> getAvisBySite(Long siteId);
}
