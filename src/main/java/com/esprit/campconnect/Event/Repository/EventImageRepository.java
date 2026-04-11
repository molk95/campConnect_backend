package com.esprit.campconnect.Event.Repository;

import com.esprit.campconnect.Event.Entity.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventImageRepository extends JpaRepository<EventImage, Long> {

    List<EventImage> findByEventId(Long eventId);

    java.util.Optional<EventImage> findByIdAndEventId(Long id, Long eventId);

    @Query("SELECT COALESCE(MAX(ei.displayOrder), -1) FROM EventImage ei WHERE ei.event.id = :eventId")
    Integer getMaxDisplayOrderForEvent(@Param("eventId") Long eventId);
}
