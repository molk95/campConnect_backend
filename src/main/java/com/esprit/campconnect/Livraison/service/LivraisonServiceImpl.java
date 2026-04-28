package com.esprit.campconnect.Livraison.service;

import com.esprit.campconnect.Livraison.demo.service.OpenStreetMapGeocodingService;
import com.esprit.campconnect.Livraison.dto.*;
import com.esprit.campconnect.Livraison.entity.*;
import com.esprit.campconnect.Livraison.repository.LivraisonCommandeRepository;
import com.esprit.campconnect.Livraison.repository.LivraisonRepository;
import com.esprit.campconnect.Livraison.repository.LivreurLocationRepository;
import com.esprit.campconnect.MarketPlace.Commande.Entity.Commande;
import com.esprit.campconnect.MarketPlace.Commande.Entity.StatutCommande;
import com.esprit.campconnect.MarketPlace.Commande.Repository.CommandeRepository;
import com.esprit.campconnect.Restauration.Entity.CommandeRepas;
import com.esprit.campconnect.Restauration.Enum.StatutCommandeRepas;
import com.esprit.campconnect.Restauration.Repository.CommandeRepasRepository;
import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LivraisonServiceImpl implements ILivraisonService {

    private final LivraisonRepository livraisonRepository;
    private final LivraisonCommandeRepository livraisonCommandeRepository;
    private final CommandeRepository commandeRepository;
    private final CommandeRepasRepository commandeRepasRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final LivreurLocationRepository livreurLocationRepository;
    private final OpenStreetMapGeocodingService geocodingService;

    private static final int MAX_ACTIVE_LIVRAISONS_PER_LIVREUR = 999999995;

    private Utilisateur getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName().equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName();

        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    private LivraisonResponse mapToResponse(Livraison l) {
        LivraisonCommande lien = l.getLivraisonCommande();

        return new LivraisonResponse(
                l.getIdLivraison(),
                l.getDateDepart(),
                l.getDateLivraisonEffective(),
                l.getAdresseLivraison(),
                l.getStatut() != null ? l.getStatut().name() : null,
                l.getPreuveLivraison(),
                l.getCommentaire(),

                l.getLivreur() != null ? l.getLivreur().getId() : null,
                l.getLivreur() != null ? l.getLivreur().getNom() : null,
                l.getLivreur() != null ? l.getLivreur().getEmail() : null,

                l.getLatitudeLivraison(),
                l.getLongitudeLivraison(),

                lien != null ? lien.getCommandeId() : null,
                lien != null ? lien.getTypeCommande() : null

        );
    }

    private LivraisonResponse createLivraisonInternal(LivraisonCreateRequest request) {
        if (request.getCommandeId() == null) {
            throw new RuntimeException("commandeId is required");
        }

        if (request.getTypeCommande() == null) {
            throw new RuntimeException("typeCommande is required");
        }

        if (request.getAdresseLivraison() == null || request.getAdresseLivraison().isBlank()) {
            throw new RuntimeException("adresseLivraison is required");
        }

        if (livraisonCommandeRepository.existsByCommandeIdAndTypeCommande(
                request.getCommandeId(),
                request.getTypeCommande())) {
            throw new RuntimeException("This order already has a livraison");
        }

        if (request.getTypeCommande() == TypeCommandeLivraison.CLASSIQUE) {
            Commande commande = commandeRepository.findById(request.getCommandeId())
                    .orElseThrow(() -> new RuntimeException("Commande not found"));

            if (commande.getStatut() != StatutCommande.PAYEE) {
                throw new RuntimeException("Delivery can only be created for PAYEE orders");
            }
        } else {
            CommandeRepas commandeRepas = commandeRepasRepository.findById(request.getCommandeId())
                    .orElseThrow(() -> new RuntimeException("CommandeRepas not found"));

            if (commandeRepas.getStatut() != StatutCommandeRepas.CONFIRMEE) {
                throw new RuntimeException("Delivery can only be created for CONFIRMEE food orders");
            }
        }

        Livraison livraison = new Livraison();
        livraison.setAdresseLivraison(request.getAdresseLivraison());
        livraison.setCommentaire(request.getCommentaire());
        livraison.setStatut(StatutLivraison.PLANIFIEE);

        livraison.setLatitudeLivraison(request.getLatitudeLivraison());
        livraison.setLongitudeLivraison(request.getLongitudeLivraison());

        livraison.setDistanceKm(request.getDistanceKm());
        livraison.setPoidsKg(request.getPoidsKg());
        livraison.setFraisDistance(request.getFraisDistance());
        livraison.setFraisPoids(request.getFraisPoids());
        livraison.setFraisMeteo(request.getFraisMeteo());
        livraison.setFraisLivraisonTotal(request.getFraisLivraisonTotal());
        livraison.setMeteoCondition(request.getMeteoCondition());

        LivraisonCommande livraisonCommande = new LivraisonCommande();
        livraisonCommande.setCommandeId(request.getCommandeId());
        livraisonCommande.setTypeCommande(request.getTypeCommande());
        livraisonCommande.setLivraison(livraison);

        livraison.setLivraisonCommande(livraisonCommande);

        Livraison saved = livraisonRepository.save(livraison);
        return mapToResponse(saved);
    }

    @Override
    public LivraisonResponse createLivraison(LivraisonCreateRequest request) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMINISTRATEUR) {
            throw new RuntimeException("Only ADMIN can create a livraison");
        }

        return createLivraisonInternal(request);
    }

    private double calculateDistanceMeters(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {
        final int earthRadiusMeters = 6371000;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusMeters * c;
    }

    @Override
    public LivraisonResponse updateStatus(Long idLivraison, LivraisonStatusUpdateRequest request) {
        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new RuntimeException("Livraison not found"));

        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.LIVREUR) {
            throw new RuntimeException("Only LIVREUR can update delivery status");
        }

        if (livraison.getLivreur() == null || !livraison.getLivreur().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not assigned to this livraison");
        }

        if (request.getStatut() == null) {
            throw new RuntimeException("Statut is required");
        }

        StatutLivraison currentStatus = livraison.getStatut();
        StatutLivraison nextStatus = request.getStatut();

        if (currentStatus == StatutLivraison.LIVREE) {
            throw new RuntimeException("Cannot modify a delivered livraison");
        }

        if (nextStatus != StatutLivraison.EN_COURS && nextStatus != StatutLivraison.LIVREE) {
            throw new RuntimeException("Livreur can only start or mark a livraison as delivered");
        }

        if (currentStatus == StatutLivraison.PLANIFIEE && nextStatus != StatutLivraison.EN_COURS) {
            throw new RuntimeException("Livraison must be started before being delivered");
        }

        if (currentStatus == StatutLivraison.EN_COURS && nextStatus != StatutLivraison.LIVREE) {
            throw new RuntimeException("Livraison in progress can only be marked as delivered");
        }

        if (nextStatus == StatutLivraison.LIVREE &&
                (request.getPreuveLivraison() == null || request.getPreuveLivraison().isBlank())) {
            throw new RuntimeException("Delivery proof is required when marking as delivered");
        }

        if (nextStatus == StatutLivraison.LIVREE) {
            if (request.getCurrentLatitude() == null || request.getCurrentLongitude() == null) {
                throw new RuntimeException("Current location is required to confirm delivery");
            }

            if (livraison.getLatitudeLivraison() == null || livraison.getLongitudeLivraison() == null) {
                throw new RuntimeException("Delivery destination location is missing");
            }

            double distanceMeters = calculateDistanceMeters(
                    request.getCurrentLatitude(),
                    request.getCurrentLongitude(),
                    livraison.getLatitudeLivraison(),
                    livraison.getLongitudeLivraison()
            );

            if (distanceMeters > 100) {
                throw new RuntimeException("You must be within 100 meters of the delivery destination");
            }
        }

        livraison.setStatut(nextStatus);
        livraison.setCommentaire(request.getCommentaire());
        livraison.setPreuveLivraison(request.getPreuveLivraison());

        if (nextStatus == StatutLivraison.EN_COURS && livraison.getDateDepart() == null) {
            livraison.setDateDepart(LocalDate.now());
        }

        if (nextStatus == StatutLivraison.LIVREE) {
            livraison.setDateLivraisonEffective(LocalDate.now());

            LivraisonCommande lien = livraison.getLivraisonCommande();
            if (lien == null) {
                throw new RuntimeException("LivraisonCommande not found");
            }

            if (lien.getTypeCommande() == TypeCommandeLivraison.CLASSIQUE) {
                Commande commande = commandeRepository.findById(lien.getCommandeId())
                        .orElseThrow(() -> new RuntimeException("Commande not found"));
                commande.setStatut(StatutCommande.LIVREE);
                commandeRepository.save(commande);
            } else {
                CommandeRepas commandeRepas = commandeRepasRepository.findById(lien.getCommandeId())
                        .orElseThrow(() -> new RuntimeException("CommandeRepas not found"));
                commandeRepas.setStatut(StatutCommandeRepas.LIVREE);
                commandeRepasRepository.save(commandeRepas);
            }
        }

        Livraison updated = livraisonRepository.save(livraison);
        return mapToResponse(updated);
    }

    @Override
    public List<LivraisonResponse> getMyLivraisons() {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.LIVREUR) {
            throw new RuntimeException("Only LIVREUR can access their livraisons");
        }

        return livraisonRepository.findByLivreur_Id(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<LivraisonResponse> getAll() {
        return livraisonRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public LivraisonStatsResponse getMyStats() {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.LIVREUR) {
            throw new RuntimeException("Only LIVREUR can access delivery stats");
        }

        Long livreurId = currentUser.getId();

        return new LivraisonStatsResponse(
                livraisonRepository.countByLivreur_Id(livreurId),
                livraisonRepository.countByLivreur_IdAndStatut(livreurId, StatutLivraison.PLANIFIEE),
                livraisonRepository.countByLivreur_IdAndStatut(livreurId, StatutLivraison.EN_COURS),
                livraisonRepository.countByLivreur_IdAndStatut(livreurId, StatutLivraison.LIVREE),
                livraisonRepository.countByLivreur_IdAndStatut(livreurId, StatutLivraison.ECHOUEE),
                livraisonRepository.countByLivreur_IdAndStatut(livreurId, StatutLivraison.RETOURNEE)
        );
    }

    @Override
    public List<AvailableOrderForLivraisonResponse> getAvailableClassicOrders() {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMINISTRATEUR) {
            throw new RuntimeException("Only ADMIN can access available classic orders");
        }

        return commandeRepository.findAll()
                .stream()
                .filter(commande ->
                        commande.getStatut() == StatutCommande.PAYEE &&
                                !livraisonCommandeRepository.existsByCommandeIdAndTypeCommande(
                                        commande.getIdCommande(),
                                        TypeCommandeLivraison.CLASSIQUE
                                )
                )
                .map(commande -> new AvailableOrderForLivraisonResponse(
                        commande.getIdCommande(),
                        TypeCommandeLivraison.CLASSIQUE,
                        commande.getDateCommande(),
                        commande.getStatut() != null ? commande.getStatut().name() : null,
                        commande.getTotalCommande(),
                        commande.getUtilisateur() != null ? commande.getUtilisateur().getId() : null,
                        commande.getUtilisateur() != null ? commande.getUtilisateur().getEmail() : null,
                        commande.getUtilisateur() != null ? commande.getUtilisateur().getNom() : null,
                        commande.getUtilisateur() != null ? commande.getUtilisateur().getTelephone() : null
                ))
                .toList();
    }

    @Override
    public List<AvailableOrderForLivraisonResponse> getAvailableRepasOrders() {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMINISTRATEUR) {
            throw new RuntimeException("Only ADMIN can access available food orders");
        }

        return commandeRepasRepository.findAll()
                .stream()
                .filter(commandeRepas ->
                        commandeRepas.getStatut() == StatutCommandeRepas.CONFIRMEE &&
                                !livraisonCommandeRepository.existsByCommandeIdAndTypeCommande(
                                        commandeRepas.getId(),
                                        TypeCommandeLivraison.REPAS
                                )
                )
                .map(commandeRepas -> new AvailableOrderForLivraisonResponse(
                        commandeRepas.getId(),
                        TypeCommandeLivraison.REPAS,
                        commandeRepas.getDateCommande(),
                        commandeRepas.getStatut() != null ? commandeRepas.getStatut().name() : null,
                        commandeRepas.getMontantTotal(),
                        commandeRepas.getUtilisateur() != null ? commandeRepas.getUtilisateur().getId() : null,
                        commandeRepas.getUtilisateur() != null ? commandeRepas.getUtilisateur().getEmail() : null,
                        commandeRepas.getUtilisateur() != null ? commandeRepas.getUtilisateur().getNom() : null,
                        commandeRepas.getUtilisateur() != null ? commandeRepas.getUtilisateur().getTelephone() : null
                ))
                .toList();
    }

    @Override
    public List<Utilisateur> getLivreurs() {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMINISTRATEUR) {
            throw new RuntimeException("Only ADMIN can access delivery people");
        }

        return utilisateurRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == Role.LIVREUR)
                .toList();
    }

    @Override
    public LivraisonResponse createLivraisonAfterPayment(LivraisonCreateRequest request) {
        return createLivraisonInternal(request);
    }

    @Override
    public LivreurLocationResponse updateLivreurLocation(
            Long idLivraison,
            LivreurLocationUpdateRequest request
    ) {
        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new RuntimeException("Livraison not found"));

        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.LIVREUR) {
            throw new RuntimeException("Only LIVREUR can update live location");
        }

        if (livraison.getLivreur() == null ||
                !livraison.getLivreur().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not assigned to this livraison");
        }

        if (livraison.getStatut() != StatutLivraison.EN_COURS) {
            throw new RuntimeException("Location can only be updated for delivery in progress");
        }

        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new RuntimeException("Latitude and longitude are required");
        }

        LivreurLocation location = livreurLocationRepository
                .findByLivraisonId(idLivraison)
                .orElseGet(LivreurLocation::new);

        location.setLivraisonId(idLivraison);
        location.setLivreurId(currentUser.getId());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setUpdatedAt(LocalDateTime.now());

        LivreurLocation saved = livreurLocationRepository.save(location);

        return new LivreurLocationResponse(
                saved.getLivraisonId(),
                saved.getLivreurId(),
                saved.getLatitude(),
                saved.getLongitude(),
                saved.getUpdatedAt()
        );
    }

    @Override
    public LivreurLocationResponse getLivreurLocation(Long idLivraison) {
        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new RuntimeException("Livraison not found"));

        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CLIENT &&
                currentUser.getRole() != Role.ADMINISTRATEUR &&
                currentUser.getRole() != Role.LIVREUR) {
            throw new RuntimeException("You are not allowed to track this delivery");
        }

        LivreurLocation location = livreurLocationRepository
                .findByLivraisonId(idLivraison)
                .orElseThrow(() -> new RuntimeException("Livreur location not available yet"));

        return new LivreurLocationResponse(
                location.getLivraisonId(),
                location.getLivreurId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getUpdatedAt()
        );
    }

    @Override
    public List<LivraisonResponse> getMyClientLivraisons() {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CLIENT) {
            throw new RuntimeException("Only CLIENT can access their deliveries");
        }

        List<Livraison> livraisons = livraisonRepository.findClientLivraisons(currentUser.getId());

        return livraisons.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public LivraisonResponse getLivraisonById(Long idLivraison) {
        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new RuntimeException("Livraison not found"));

        if (livraison.getLatitudeLivraison() == null || livraison.getLongitudeLivraison() == null) {
            try {
                OpenStreetMapGeocodingService.GeocodingResult location =
                        geocodingService.geocode(livraison.getAdresseLivraison());

                livraison.setLatitudeLivraison(location.latitude());
                livraison.setLongitudeLivraison(location.longitude());

                livraison = livraisonRepository.save(livraison);
            } catch (Exception ignored) {
                // Keep response usable; frontend can show details without map
            }
        }

        return mapToResponse(livraison);
    }

    @Override
    public LivraisonResponse assignLivreur(Long idLivraison, Long livreurId) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMINISTRATEUR) {
            throw new RuntimeException("Only ADMIN can assign a livreur");
        }

        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new RuntimeException("Livraison not found"));

        if (livraison.getLivreur() != null) {
            throw new RuntimeException("Livreur already assigned");
        }

        if (livraison.getStatut() == StatutLivraison.LIVREE) {
            throw new RuntimeException("Cannot assign livreur to a delivered livraison");
        }

        Utilisateur livreur = utilisateurRepository.findById(livreurId)
                .orElseThrow(() -> new RuntimeException("Livreur not found"));

        if (livreur.getRole() != Role.LIVREUR) {
            throw new RuntimeException("User is not a LIVREUR");
        }

        long activeLivraisons = livraisonRepository.countByLivreur_IdAndStatutIn(
                livreurId,
                List.of(StatutLivraison.PLANIFIEE, StatutLivraison.EN_COURS)
        );

        if (activeLivraisons >= MAX_ACTIVE_LIVRAISONS_PER_LIVREUR) {
            throw new RuntimeException("This livreur already reached the maximum number of active livraisons");
        }

        livraison.setLivreur(livreur);

        Livraison saved = livraisonRepository.save(livraison);
        return mapToResponse(saved);
    }
}