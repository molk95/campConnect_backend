package com.esprit.campconnect.siteCamping;

import java.util.List;

public interface ISiteCampingService {
    SiteCamping addSiteCamping(SiteCamping siteCamping);

    SiteCamping patchSiteCamping(Long idSite, SiteCamping updatedData);

    SiteCamping getSiteCampingById(Long idSite);

    List<SiteCamping> getAllSiteCampings();

    void deleteSiteCamping(Long idSite);
}
