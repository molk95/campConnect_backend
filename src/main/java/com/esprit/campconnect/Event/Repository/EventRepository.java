package com.esprit.campconnect.Event.Repository;

import com.esprit.campconnect.Event.Entity.Event;
import com.esprit.campconnect.Event.Enum.EventCategory;
import com.esprit.campconnect.Event.Enum.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Find events by category
    List<Event> findByCategorie(EventCategory categorie);

    // Find events by status
    List<Event> findByStatut(EventStatus statut);

    // Find upcoming events
    List<Event> findByDateDebutAfterOrderByDateDebut(LocalDateTime dateDebut);

    // Find events by organizer
    List<Event> findByOrganizerId(Long organizerId);

    // Find events between date range
    @Query("SELECT e FROM Event e WHERE e.dateDebut >= :dateDebut AND e.dateFin <= :dateFin")
    List<Event> findEventsBetweenDates(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );

    // Find available events (not fully booked)
    @Query("SELECT e FROM Event e WHERE e.statut = 'SCHEDULED' AND SIZE(e.reservations) < e.capaciteMax")
    List<Event> findAvailableEvents();

    // Find events with available spots
    @Query(value = "SELECT e FROM Event e WHERE e.capaciteMax > (SELECT COUNT(r) FROM Reservation r WHERE r.event.id = e.id AND r.statut IN ('CONFIRMED', 'PAID'))")
    List<Event> findEventsWithAvailableSpots();

    // Search events by title or description
    @Query("SELECT e FROM Event e WHERE LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByKeyword(@Param("keyword") String keyword);

    // Find events in specific location
    List<Event> findByLieuContainingIgnoreCase(String lieu);

    // Find events organized by user with specific status
    @Query("SELECT e FROM Event e WHERE e.organizer.id = :organizerId AND e.statut = :statut")
    List<Event> findByOrganizerAndStatus(
            @Param("organizerId") Long organizerId,
            @Param("statut") EventStatus statut
    );
}
