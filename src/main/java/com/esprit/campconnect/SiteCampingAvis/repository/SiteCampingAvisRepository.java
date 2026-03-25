package com.esprit.campconnect.SiteCampingAvis.repository;

import com.esprit.campconnect.SiteCampingAvis.entity.SiteCampingAvis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteCampingAvisRepository extends JpaRepository<SiteCampingAvis, Long> {
}
