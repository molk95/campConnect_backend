package com.esprit.campconnect.siteCamping.repository;

import com.esprit.campconnect.siteCamping.entity.SiteCamping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteCampingRepository extends JpaRepository<SiteCamping, Long> {
}
