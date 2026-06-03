package com.esprit.campconnect.InscriptionSite.service;

import com.esprit.campconnect.InscriptionSite.dto.InscriptionCheckoutResponse;
import com.esprit.campconnect.InscriptionSite.dto.InscriptionSiteCreateRequest;
import com.esprit.campconnect.InscriptionSite.entity.InscriptionSite;
import com.esprit.campconnect.InscriptionSite.entity.StatutInscription;
import com.esprit.campconnect.InscriptionSite.repository.InscriptionSiteRepository;
import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import com.esprit.campconnect.siteCamping.entity.SiteCamping;
import com.esprit.campconnect.siteCamping.entity.StatutDispo;
import com.esprit.campconnect.siteCamping.repository.SiteCampingRepository;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscriptionSiteServiceImpTest {

    @Mock private InscriptionStripeService inscriptionStripeService;
    @Mock private InscriptionSiteRepository inscriptionSiteRepository;
    @Mock private SiteCampingRepository siteCampingRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    // We can mock others as needed or let them be null if not reached
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private InscriptionSiteServiceImp inscriptionSiteService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(Long id, String email) {
        Utilisateur fakeUser = new Utilisateur();
        fakeUser.setId(id);
        fakeUser.setEmail(email);
        fakeUser.setRole(Role.CLIENT);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(email);
        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(fakeUser));
    }

    @Test
    void addInscriptionSite_ShouldCreatePendingBooking_WhenCapacityIsAvailable() {
        // Arrange
        mockAuthenticatedUser(1L, "client@test.com");

        SiteCamping site = new SiteCamping();
        site.setIdSite(10L);
        site.setCapacite(20);
        site.setStatutDispo(StatutDispo.AVAILABLE);

        when(siteCampingRepository.findById(10L)).thenReturn(Optional.of(site));
        when(inscriptionSiteRepository.sumGuestsBySiteAndStatutAndDateOverlap(any(), any(), any(), any()))
                .thenReturn(5); // 5 guests already reserved

        InscriptionSiteCreateRequest request = new InscriptionSiteCreateRequest();
        request.setSiteId(10L);
        request.setNumberOfGuests(2);
        request.setDateDebut(LocalDate.now().plusDays(1));
        request.setDateFin(LocalDate.now().plusDays(5));

        InscriptionSite savedInscription = new InscriptionSite();
        savedInscription.setIdInscription(1L);
        savedInscription.setStatut(StatutInscription.PENDING);
        when(inscriptionSiteRepository.save(any(InscriptionSite.class))).thenReturn(savedInscription);

        // Mock Stripe Session
        Session mockSession = mock(Session.class);
        when(mockSession.getUrl()).thenReturn("http://stripe.com/session");
        when(inscriptionStripeService.createCheckoutSession(any())).thenReturn(mockSession);

        // Act
        InscriptionCheckoutResponse response = inscriptionSiteService.addInscriptionSite(request);

        // Assert
        assertThat(response.getInscription().getIdInscription()).isEqualTo(1L);
        assertThat(response.getInscription().getStatut()).isEqualTo(StatutInscription.PENDING);
        verify(inscriptionSiteRepository).save(any(InscriptionSite.class));
    }

    @Test
    void addInscriptionSite_ShouldThrowException_WhenGuestsExceedCapacity() {
        // Arrange
        SiteCamping site = new SiteCamping();
        site.setIdSite(10L);
        site.setCapacite(10); // Capacity 10
        when(siteCampingRepository.findById(10L)).thenReturn(Optional.of(site));

        // 8 already reserved + 5 new > 10
        when(inscriptionSiteRepository.sumGuestsBySiteAndStatutAndDateOverlap(any(), any(), any(), any()))
                .thenReturn(8);

        InscriptionSiteCreateRequest request = new InscriptionSiteCreateRequest();
        request.setSiteId(10L);
        request.setNumberOfGuests(5);
        request.setDateDebut(LocalDate.now().plusDays(1));
        request.setDateFin(LocalDate.now().plusDays(5));

        // Act & Assert
        assertThatThrownBy(() -> inscriptionSiteService.addInscriptionSite(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("numberOfGuests exceeds remaining capacity");
    }

    @Test
    void confirmPayment_ShouldSetStatusToConfirmed() {
        // Arrange
        InscriptionSite inscription = new InscriptionSite();
        inscription.setIdInscription(1L);
        inscription.setStatut(StatutInscription.PENDING);

        SiteCamping site = new SiteCamping();
        site.setCapacite(20);
        inscription.setSiteCamping(site);

        when(inscriptionSiteRepository.findById(1L)).thenReturn(Optional.of(inscription));
        when(inscriptionSiteRepository.save(any())).thenReturn(inscription);

        // Act
        inscriptionSiteService.confirmPayment(1L);

        // Assert
        assertThat(inscription.getStatut()).isEqualTo(StatutInscription.CONFIRMED);
        verify(inscriptionSiteRepository).save(inscription);
    }
}