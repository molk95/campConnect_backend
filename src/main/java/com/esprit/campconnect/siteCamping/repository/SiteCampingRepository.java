package com.esprit.campconnect.siteCamping.repository;

import com.esprit.campconnect.siteCamping.entity.SiteCamping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteCampingRepository extends JpaRepository<SiteCamping, Long> {
    List<SiteCamping> findByOwner_Id(Long ownerId);
}
