package com.esprit.campconnect.Livraison.service;

import com.esprit.campconnect.Livraison.demo.service.OpenStreetMapGeocodingService;
import com.esprit.campconnect.Livraison.dto.LivraisonCreateRequest;
import com.esprit.campconnect.Livraison.dto.LivraisonResponse;
import com.esprit.campconnect.Livraison.dto.LivraisonStatusUpdateRequest;
import com.esprit.campconnect.Livraison.entity.Livraison;
import com.esprit.campconnect.Livraison.entity.LivraisonCommande;
import com.esprit.campconnect.Livraison.entity.StatutLivraison;
import com.esprit.campconnect.Livraison.entity.TypeCommandeLivraison;
import com.esprit.campconnect.Livraison.repository.*;
import com.esprit.campconnect.MarketPlace.Commande.Entity.Commande;
import com.esprit.campconnect.MarketPlace.Commande.Entity.StatutCommande;
import com.esprit.campconnect.MarketPlace.Commande.Repository.CommandeRepository;
import com.esprit.campconnect.Restauration.Repository.CommandeRepasRepository;
import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import com.esprit.campconnect.common.EmailService;
import com.esprit.campconnect.common.LivraisonEmailTemplateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivraisonServiceImplTest {

    @Mock private EmailService emailService;
    @Mock private LivraisonEmailTemplateService emailTemplateService;
    @Mock private LivraisonRepository livraisonRepository;
    @Mock private LivraisonCommandeRepository livraisonCommandeRepository;
    @Mock private CommandeRepository commandeRepository;
    @Mock private CommandeRepasRepository commandeRepasRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private LivreurLocationRepository livreurLocationRepository;
    @Mock private OpenStreetMapGeocodingService geocodingService;
    @Mock private LivreurWalletRepository walletRepository;
    @Mock private LivreurTipRepository tipRepository;
    @Mock private LivreurWithdrawRepository withdrawRepository;

    // Security Mocks
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private LivraisonServiceImpl livraisonService;

    @AfterEach
    void tearDown() {
        // Clear security context after each test
        SecurityContextHolder.clearContext();
    }

    // --- Helper to mock the logged-in user ---
    private void mockAuthenticatedUser(Long id, String email, Role role) {
        Utilisateur fakeUser = new Utilisateur();
        fakeUser.setId(id);
        fakeUser.setEmail(email);
        fakeUser.setRole(role);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(fakeUser));
    }

    // ==========================================
    // TEST: Create Livraison
    // ==========================================

    @Test
    void createLivraison_ShouldCreateSuccessfully_WhenAdminAndOrderIsPaid() {
        // Arrange: Log in as ADMIN
        mockAuthenticatedUser(1L, "admin@test.com", Role.ADMINISTRATEUR);

        LivraisonCreateRequest request = new LivraisonCreateRequest();
        request.setCommandeId(10L);
        request.setTypeCommande(TypeCommandeLivraison.CLASSIQUE);
        request.setAdresseLivraison("123 Camp Street");

        Commande fakeCommande = new Commande();
        fakeCommande.setIdCommande(10L);
        fakeCommande.setStatut(StatutCommande.PAYEE); // Requirement: Must be PAYEE

        when(livraisonCommandeRepository.existsByCommandeIdAndTypeCommande(10L, TypeCommandeLivraison.CLASSIQUE))
                .thenReturn(false);
        when(commandeRepository.findById(10L)).thenReturn(Optional.of(fakeCommande));

        Livraison savedLivraison = new Livraison();
        savedLivraison.setIdLivraison(99L);
        savedLivraison.setAdresseLivraison("123 Camp Street");
        savedLivraison.setStatut(StatutLivraison.PLANIFIEE);

        when(livraisonRepository.save(any(Livraison.class))).thenReturn(savedLivraison);

        // Act
        LivraisonResponse response = livraisonService.createLivraison(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIdLivraison()).isEqualTo(99L);
        assertThat(response.getStatut()).isEqualTo("PLANIFIEE");
        verify(livraisonRepository).save(any(Livraison.class));
    }

    // ==========================================
    // TEST: Assign Livreur
    // ==========================================

    @Test
    void assignLivreur_ShouldAssignSuccessfully_WhenAdmin() {
        // Arrange: Log in as ADMIN
        mockAuthenticatedUser(1L, "admin@test.com", Role.ADMINISTRATEUR);

        Livraison fakeLivraison = new Livraison();
        fakeLivraison.setIdLivraison(50L);
        fakeLivraison.setStatut(StatutLivraison.PLANIFIEE);

        Utilisateur fakeDriver = new Utilisateur();
        fakeDriver.setId(2L);
        fakeDriver.setRole(Role.LIVREUR); // Requirement: User must be a driver

        when(livraisonRepository.findById(50L)).thenReturn(Optional.of(fakeLivraison));
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(fakeDriver));
        when(livraisonRepository.save(any(Livraison.class))).thenReturn(fakeLivraison);

        // Act
        LivraisonResponse response = livraisonService.assignLivreur(50L, 2L);

        // Assert
        assertThat(fakeLivraison.getLivreur()).isNotNull();
        assertThat(fakeLivraison.getLivreur().getId()).isEqualTo(2L);
        verify(livraisonRepository).save(fakeLivraison);
    }

    // ==========================================
    // TEST: Update Status (Delivering the order)
    // ==========================================

    @Test
    void updateStatus_ShouldUpdateToLivreeAndCompleteOrder_WhenLivreurIsAssigned() {
        // Arrange: Log in as the ASSIGNED DRIVER
        Long livreurId = 3L;
        mockAuthenticatedUser(livreurId, "driver@test.com", Role.LIVREUR);

        Utilisateur assignedDriver = new Utilisateur();
        assignedDriver.setId(livreurId);

        Livraison fakeLivraison = new Livraison();
        fakeLivraison.setIdLivraison(50L);
        fakeLivraison.setStatut(StatutLivraison.EN_COURS);
        fakeLivraison.setLivreur(assignedDriver); // Driver is assigned!

        LivraisonCommande lc = new LivraisonCommande();
        lc.setCommandeId(20L);
        lc.setTypeCommande(TypeCommandeLivraison.CLASSIQUE);
        fakeLivraison.setLivraisonCommande(lc);

        Commande fakeCommande = new Commande();
        fakeCommande.setIdCommande(20L);
        fakeCommande.setStatut(StatutCommande.PAYEE);

        when(livraisonRepository.findById(50L)).thenReturn(Optional.of(fakeLivraison));
        when(commandeRepository.findById(20L)).thenReturn(Optional.of(fakeCommande));
        when(livraisonRepository.save(any(Livraison.class))).thenReturn(fakeLivraison);

        LivraisonStatusUpdateRequest request = new LivraisonStatusUpdateRequest();
        request.setStatut(StatutLivraison.LIVREE);
        request.setPreuveLivraison("Package left at door");

        // Act
        LivraisonResponse response = livraisonService.updateStatus(50L, request);

        // Assert
        assertThat(response.getStatut()).isEqualTo("LIVREE");
        assertThat(fakeLivraison.getDateLivraisonEffective()).isNotNull();
        assertThat(fakeCommande.getStatut()).isEqualTo(StatutCommande.LIVREE); // Parent order updated!
        verify(commandeRepository).save(fakeCommande);
    }

    @Test
    void updateStatus_ShouldThrowException_WhenWrongLivreurTriesToUpdate() {
        // Arrange: Log in as Driver 99, but delivery belongs to Driver 3
        mockAuthenticatedUser(99L, "imposter@test.com", Role.LIVREUR);

        Utilisateur realDriver = new Utilisateur();
        realDriver.setId(3L);

        Livraison fakeLivraison = new Livraison();
        fakeLivraison.setIdLivraison(50L);
        fakeLivraison.setLivreur(realDriver);

        when(livraisonRepository.findById(50L)).thenReturn(Optional.of(fakeLivraison));

        LivraisonStatusUpdateRequest request = new LivraisonStatusUpdateRequest();
        request.setStatut(StatutLivraison.LIVREE);

        // Act & Assert
        assertThatThrownBy(() -> livraisonService.updateStatus(50L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You are not assigned to this livraison");
    }
}