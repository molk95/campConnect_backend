package com.esprit.campconnect.Restauration.Repository;

import com.esprit.campconnect.Restauration.Entity.CommandeRepasNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CommandeRepasNotificationRepository
        extends JpaRepository<CommandeRepasNotification, Long> {

    List<CommandeRepasNotification> findByDestinataireIdOrderByDateCreationDesc(
            Long destinataireId);

    long countByDestinataireIdAndReadFalse(Long destinataireId);

    Optional<CommandeRepasNotification> findByIdAndDestinataireId(
            Long id, Long destinataireId);

    @Modifying
    @Query("UPDATE CommandeRepasNotification n " +
            "SET n.read = true, n.readAt = CURRENT_TIMESTAMP " +
            "WHERE n.destinataireId = :userId AND n.read = false")
    void markAllAsReadByUserId(Long userId);
}