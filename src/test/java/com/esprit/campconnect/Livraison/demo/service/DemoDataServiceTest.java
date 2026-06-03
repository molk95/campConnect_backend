package com.esprit.campconnect.Livraison.demo.service;

import com.esprit.campconnect.Livraison.demo.dto.DemoCheckoutItemRequest;
import com.esprit.campconnect.Livraison.demo.dto.DemoCheckoutRequest;
import com.esprit.campconnect.Livraison.demo.dto.DemoCheckoutResponse;
import com.esprit.campconnect.Livraison.demo.dto.LivraisonFeeResponse;
import com.esprit.campconnect.Livraison.entity.TypeCommandeLivraison;
import com.esprit.campconnect.MarketPlace.Commande.Entity.Commande;
import com.esprit.campconnect.MarketPlace.Commande.Entity.StatutCommande;
import com.esprit.campconnect.MarketPlace.Commande.Repository.CommandeRepository;
import com.esprit.campconnect.Restauration.Repository.CommandeRepasRepository;
import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoDataServiceTest {

    @Mock private OpenStreetMapGeocodingService geocodingService;
    @Mock private CommandeRepository commandeRepository;
    @Mock private CommandeRepasRepository commandeRepasRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private LivraisonPricingService pricingService;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private DemoDataService demoDataService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- Helper to mock the logged-in user ---
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

    // ==========================================
    // TEST: Basic Commande Creation
    // ==========================================

    @Test
    void createCommande_ShouldSaveAndReturnCommande_WithCorrectTotalAndStatus() {
        // Arrange
        mockAuthenticatedUser(1L, "client@test.com");

        Commande savedCommande = new Commande();
        savedCommande.setIdCommande(100L);
        savedCommande.setTotalCommande(150.0);
        savedCommande.setStatut(StatutCommande.EN_ATTENTE);

        when(commandeRepository.save(any(Commande.class))).thenReturn(savedCommande);

        // Act
        Commande result = demoDataService.createCommande(150.0);

        // Assert
        assertThat(result.getIdCommande()).isEqualTo(100L);
        assertThat(result.getTotalCommande()).isEqualTo(150.0);
        assertThat(result.getStatut()).isEqualTo(StatutCommande.EN_ATTENTE);
        verify(commandeRepository).save(any(Commande.class));
    }

    // ==========================================
    // TEST: Complex Checkout Flow (Classic)
    // ==========================================

    @Test
    void createClassicCheckout_ShouldCalculateFeesAndReturnCheckoutResponse_WhenValidRequest() {
        // Arrange
        mockAuthenticatedUser(2L, "buyer@test.com");

        // 1. Prepare the incoming request (Buying 2 Tents, ID: 1)
        DemoCheckoutRequest request = new DemoCheckoutRequest();

        // --- THIS IS THE FIXED PART ---
        DemoCheckoutItemRequest item = new DemoCheckoutItemRequest();
        item.setId(1L);
        item.setQuantity(2);
        // ------------------------------

        request.setItems(List.of(item));
        request.setLatitude(36.8);
        request.setLongitude(10.2);
        request.setAdresseLivraison("Lac 1, Tunis");

        // 2. Prepare the fake pricing response
        LivraisonFeeResponse fakeFeeResponse = new LivraisonFeeResponse(
                240.0, // 2 tents * 120
                3.0,   // base fee
                5.0,   // distance fee
                6.0,   // weight fee
                0.0,   // weather fee
                5.0,   // delivery fee
                254.0, // final total
                5.0,   // distance km
                5.0,   // poids kg (2 * 2.5)
                "NORMAL",
                25.0,
                0.0,
                "GREATER_TUNIS"
        );

        // Mock the pricing service calculation
        when(pricingService.calculateFee(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(fakeFeeResponse);

        // Mock saving the Commande
        Commande fakeCommande = new Commande();
        fakeCommande.setIdCommande(55L);
        fakeCommande.setStatut(StatutCommande.EN_ATTENTE);
        when(commandeRepository.save(any(Commande.class))).thenReturn(fakeCommande);

        // Act
        DemoCheckoutResponse response = demoDataService.createClassicCheckout(request);

        // Assert
        assertThat(response.getCommandeId()).isEqualTo(55L);
        assertThat(response.getTypeCommande()).isEqualTo(TypeCommandeLivraison.CLASSIQUE);
        assertThat(response.getItemsTotal()).isEqualTo(240.0);
        assertThat(response.getFinalTotal()).isEqualTo(254.0);
        assertThat(response.getPoidsKg()).isEqualTo(5.0);

        // Verify pricing was actually called with the math we expect
        verify(pricingService).calculateFee(eq(240.0), anyDouble(), eq(5.0), eq(36.8), eq(10.2));
    }
    @Test
    void createClassicCheckout_ShouldThrowException_WhenCartIsEmpty() {
        // Arrange
        DemoCheckoutRequest request = new DemoCheckoutRequest();
        request.setItems(List.of()); // Empty cart

        // Act & Assert
        assertThatThrownBy(() -> demoDataService.createClassicCheckout(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Please select at least one product");

        verify(commandeRepository, never()).save(any());
    }
}
