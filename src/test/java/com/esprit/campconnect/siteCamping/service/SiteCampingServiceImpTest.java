package com.esprit.campconnect.siteCamping.service;

import com.esprit.campconnect.InscriptionSite.entity.StatutInscription;
import com.esprit.campconnect.InscriptionSite.repository.InscriptionSiteRepository;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import com.esprit.campconnect.common.ICloudinaryService;
import com.esprit.campconnect.siteCamping.dto.SiteAvailabilityResponse;
import com.esprit.campconnect.siteCamping.dto.SiteCampingResponse;
import com.esprit.campconnect.siteCamping.entity.SiteCamping;
import com.esprit.campconnect.siteCamping.entity.StatutDispo;
import com.esprit.campconnect.siteCamping.repository.SiteCampingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SiteCampingServiceImpTest {

    // 1. Mock the dependencies (Fake the database and external services)
    @Mock
    private SiteCampingRepository siteCampingRepository;
    @Mock
    private ICloudinaryService cloudinaryService;
    @Mock
    private InscriptionSiteRepository inscriptionSiteRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;

    // 2. Inject the mocks into the service we want to test
    @InjectMocks
    private SiteCampingServiceImp siteCampingService;

    // --- TEST: getSiteCampingById ---

    @Test
    void getSiteCampingById_ShouldReturnSite_WhenSiteExists() {
        // Arrange: Prepare fake data
        SiteCamping fakeSite = new SiteCamping();
        fakeSite.setIdSite(100L);
        fakeSite.setNom("Camp Rades");
        fakeSite.setCapacite(50);
        fakeSite.setStatutDispo(StatutDispo.AVAILABLE);

        when(siteCampingRepository.findById(100L)).thenReturn(Optional.of(fakeSite));
        when(inscriptionSiteRepository.sumGuestsBySiteAndStatut(100L, StatutInscription.CONFIRMED)).thenReturn(10);

        // Act: Call the method
        SiteCampingResponse response = siteCampingService.getSiteCampingById(100L);

        // Assert: Verify the result
        assertThat(response).isNotNull();
        assertThat(response.getNom()).isEqualTo("Camp Rades");
        assertThat(response.getRemainingCapacity()).isEqualTo(40); // 50 capacity - 10 confirmed
        verify(siteCampingRepository).findById(100L);
    }

    @Test
    void getSiteCampingById_ShouldThrowException_WhenSiteDoesNotExist() {
        // Arrange
        when(siteCampingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> siteCampingService.getSiteCampingById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SiteCamping not found with id: 999");
    }

    // --- TEST: closeSiteCamping ---

    @Test
    void closeSiteCamping_ShouldChangeStatusToClosed_WhenSiteIsAvailable() {
        // Arrange
        SiteCamping fakeSite = new SiteCamping();
        fakeSite.setIdSite(1L);
        fakeSite.setStatutDispo(StatutDispo.AVAILABLE);
        fakeSite.setCapacite(50);

        when(siteCampingRepository.findById(1L)).thenReturn(Optional.of(fakeSite));
        when(siteCampingRepository.save(any(SiteCamping.class))).thenReturn(fakeSite);
        when(inscriptionSiteRepository.sumGuestsBySiteAndStatut(1L, StatutInscription.CONFIRMED)).thenReturn(0);

        // Act
        SiteCampingResponse response = siteCampingService.closeSiteCamping(1L);

        // Assert
        assertThat(response.getStatutDispo()).isEqualTo(StatutDispo.CLOSED);
        verify(siteCampingRepository).save(fakeSite); // Ensure save was called
    }

    @Test
    void closeSiteCamping_ShouldThrowException_WhenSiteIsAlreadyClosed() {
        // Arrange
        SiteCamping fakeSite = new SiteCamping();
        fakeSite.setIdSite(2L);
        fakeSite.setStatutDispo(StatutDispo.CLOSED);

        when(siteCampingRepository.findById(2L)).thenReturn(Optional.of(fakeSite));

        // Act & Assert
        assertThatThrownBy(() -> siteCampingService.closeSiteCamping(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Site is already closed");

        verify(siteCampingRepository, never()).save(any()); // Ensure save was NEVER called
    }

    // --- TEST: getAvailability ---

    @Test
    void getAvailability_ShouldCalculateCorrectRemainingCapacity() {
        // Arrange
        Long siteId = 5L;
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 10);

        SiteCamping fakeSite = new SiteCamping();
        fakeSite.setIdSite(siteId);
        fakeSite.setCapacite(100);

        when(siteCampingRepository.findById(siteId)).thenReturn(Optional.of(fakeSite));
        // Simulate that 30 people have already booked for these dates
        when(inscriptionSiteRepository.sumGuestsBySiteAndStatutAndDateOverlap(
                siteId, StatutInscription.CONFIRMED, startDate, endDate)).thenReturn(30);

        // Act
        SiteAvailabilityResponse response = siteCampingService.getAvailability(siteId, startDate, endDate);

        // Assert
        assertThat(response.getSiteId()).isEqualTo(siteId);
        assertThat(response.getCapacite()).isEqualTo(100);
        assertThat(response.getReservedGuests()).isEqualTo(30);
        assertThat(response.getRemainingCapacity()).isEqualTo(70); // 100 - 30
    }

    @Test
    void getAvailability_ShouldThrowException_WhenEndDateIsBeforeStartDate() {
        // Arrange
        Long siteId = 5L;
        LocalDate startDate = LocalDate.of(2026, 6, 10);
        LocalDate endDate = LocalDate.of(2026, 6, 1); // Invalid: ends before it starts

        SiteCamping fakeSite = new SiteCamping();
        when(siteCampingRepository.findById(siteId)).thenReturn(Optional.of(fakeSite));

        // Act & Assert
        assertThatThrownBy(() -> siteCampingService.getAvailability(siteId, startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dateFin must be after dateDebut");
    }
}