package com.esprit.campconnect.siteCamping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteCampingRepository extends JpaRepository<SiteCamping, Long> {
}
