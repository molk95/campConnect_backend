package com.esprit.campconnect.SiteCampingAvis.repository;

import com.esprit.campconnect.SiteCampingAvis.entity.SiteCampingAvis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteCampingAvisRepository extends JpaRepository<SiteCampingAvis, Long> {
    List<SiteCampingAvis> findBySiteCamping_IdSite(Long siteId);
}
