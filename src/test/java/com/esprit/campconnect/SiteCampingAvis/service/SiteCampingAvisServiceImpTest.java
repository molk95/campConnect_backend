package com.esprit.campconnect.SiteCampingAvis.service;

import com.esprit.campconnect.SiteCampingAvis.dto.SiteCampingAvisCreateRequest;
import com.esprit.campconnect.SiteCampingAvis.dto.SiteCampingAvisResponse;
import com.esprit.campconnect.SiteCampingAvis.dto.SiteCampingAvisUpdateRequest;
import com.esprit.campconnect.SiteCampingAvis.dto.SiteCampingRatingResponse;
import com.esprit.campconnect.SiteCampingAvis.entity.SiteCampingAvis;
import com.esprit.campconnect.SiteCampingAvis.repository.SiteCampingAvisRepository;
import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import com.esprit.campconnect.siteCamping.entity.SiteCamping;
import com.esprit.campconnect.siteCamping.repository.SiteCampingRepository;
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
class SiteCampingAvisServiceImpTest {

    @Mock private SiteCampingAvisRepository siteCampingAvisRepository;
    @Mock private SiteCampingRepository siteCampingRepository;
    @Mock private UtilisateurRepository utilisateurRepository;

    // Security Mocks
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private SiteCampingAvisServiceImp siteCampingAvisService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext(); // Clean up after every test
    }

    // --- Helper to mock the logged-in user ---
    private void mockAuthenticatedUser(Long id, String email, Role role) {
        Utilisateur fakeUser = new Utilisateur();
        fakeUser.setId(id);
        fakeUser.setEmail(email);
        fakeUser.setRole(role);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn(email);
        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(fakeUser));
    }

    // ==========================================
    // TESTS FOR CREATE
    // ==========================================

    @Test
    void createSiteCampingAvis_ShouldCreateSuccessfully_WhenValidRequest() {
        // Arrange
        mockAuthenticatedUser(1L, "client@test.com", Role.CLIENT);

        SiteCamping fakeSite = new SiteCamping();
        fakeSite.setIdSite(10L);
        fakeSite.setNom("Beautiful Forest Camp");

        when(siteCampingRepository.findById(10L)).thenReturn(Optional.of(fakeSite));

        SiteCampingAvisCreateRequest request = new SiteCampingAvisCreateRequest();
        request.setNote(4);
        request.setCommentaire("Great experience!");

        SiteCampingAvis savedAvis = new SiteCampingAvis();
        savedAvis.setId(100L);
        savedAvis.setNote(4);
        savedAvis.setCommentaire("Great experience!");
        savedAvis.setDateCreation(LocalDate.now());
        savedAvis.setSiteCamping(fakeSite);

        Utilisateur fakeUser = new Utilisateur();
        fakeUser.setId(1L);
        savedAvis.setUtilisateur(fakeUser);

        when(siteCampingAvisRepository.save(any(SiteCampingAvis.class))).thenReturn(savedAvis);

        // Act
        SiteCampingAvisResponse response = siteCampingAvisService.createSiteCampingAvis(10L, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getNote()).isEqualTo(4);
        verify(siteCampingAvisRepository).save(any(SiteCampingAvis.class));
    }

    @Test
    void createSiteCampingAvis_ShouldThrowException_WhenNoteIsGreaterThan5() {
        // Arrange
        SiteCamping fakeSite = new SiteCamping();
        when(siteCampingRepository.findById(10L)).thenReturn(Optional.of(fakeSite));

        SiteCampingAvisCreateRequest request = new SiteCampingAvisCreateRequest();
        request.setNote(6); // Invalid note!

        // Act & Assert
        assertThatThrownBy(() -> siteCampingAvisService.createSiteCampingAvis(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("note must be between 1 and 5");

        verify(siteCampingAvisRepository, never()).save(any());
    }

    // ==========================================
    // TESTS FOR UPDATE (PATCH)
    // ==========================================

    @Test
    void patchSiteCampingAvis_ShouldUpdateSuccessfully_WhenUserIsOwner() {
        // Arrange
        mockAuthenticatedUser(5L, "owner@test.com", Role.CLIENT); // User ID is 5

        Utilisateur owner = new Utilisateur();
        owner.setId(5L);

        SiteCamping fakeSite = new SiteCamping();
        fakeSite.setIdSite(10L);

        SiteCampingAvis existingAvis = new SiteCampingAvis();
        existingAvis.setId(50L);
        existingAvis.setNote(3);
        existingAvis.setUtilisateur(owner); // Owner matches logged-in user!
        existingAvis.setSiteCamping(fakeSite);

        when(siteCampingAvisRepository.findById(50L)).thenReturn(Optional.of(existingAvis));
        when(siteCampingAvisRepository.save(any(SiteCampingAvis.class))).thenReturn(existingAvis);

        SiteCampingAvisUpdateRequest request = new SiteCampingAvisUpdateRequest();
        request.setNote(5); // Updating note to 5
        request.setCommentaire("Changed my mind, it was awesome.");

        // Act
        SiteCampingAvisResponse response = siteCampingAvisService.patchSiteCampingAvis(50L, request);

        // Assert
        assertThat(response.getNote()).isEqualTo(5);
        assertThat(response.getCommentaire()).isEqualTo("Changed my mind, it was awesome.");
        verify(siteCampingAvisRepository).save(existingAvis);
    }

    @Test
    void patchSiteCampingAvis_ShouldThrowException_WhenUserIsNotOwner() {
        // Arrange
        mockAuthenticatedUser(99L, "imposter@test.com", Role.CLIENT); // Logged in as User 99

        Utilisateur realOwner = new Utilisateur();
        realOwner.setId(5L); // Review belongs to User 5

        SiteCampingAvis existingAvis = new SiteCampingAvis();
        existingAvis.setId(50L);
        existingAvis.setUtilisateur(realOwner);

        when(siteCampingAvisRepository.findById(50L)).thenReturn(Optional.of(existingAvis));

        SiteCampingAvisUpdateRequest request = new SiteCampingAvisUpdateRequest();
        request.setNote(4);

        // Act & Assert
        assertThatThrownBy(() -> siteCampingAvisService.patchSiteCampingAvis(50L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You are not allowed to update this review");
    }

    // ==========================================
    // TESTS FOR DELETE
    // ==========================================

    @Test
    void deleteSiteCampingAvis_ShouldDeleteSuccessfully_WhenUserIsAdmin() {
        // Arrange
        mockAuthenticatedUser(1L, "admin@test.com", Role.ADMINISTRATEUR); // Logged in as ADMIN

        Utilisateur normalUser = new Utilisateur();
        normalUser.setId(5L);

        SiteCampingAvis existingAvis = new SiteCampingAvis();
        existingAvis.setId(50L);
        existingAvis.setUtilisateur(normalUser); // Review belongs to someone else

        when(siteCampingAvisRepository.findById(50L)).thenReturn(Optional.of(existingAvis));

        // Act
        siteCampingAvisService.deleteSiteCampingAvis(50L);

        // Assert
        verify(siteCampingAvisRepository).delete(existingAvis); // Admin should be able to delete it!
    }

    // ==========================================
    // TESTS FOR AVERAGE RATING
    // ==========================================

    @Test
    void getAverageRatingBySite_ShouldReturnRoundedAverage_WhenSiteExists() {
        // Arrange
        when(siteCampingRepository.existsById(10L)).thenReturn(true);
        when(siteCampingAvisRepository.getAverageRatingBySiteId(10L)).thenReturn(4.567); // Unrounded
        when(siteCampingAvisRepository.countRatingsBySiteId(10L)).thenReturn(15L);

        // Act
        SiteCampingRatingResponse response = siteCampingAvisService.getAverageRatingBySite(10L);

        // Assert
        assertThat(response.getAverageRating()).isEqualTo(4.6); // Should round up
        assertThat(response.getTotalRatings()).isEqualTo(15L);
    }
}