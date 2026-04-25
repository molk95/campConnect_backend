package com.esprit.campconnect.Livraison.service;

import com.esprit.campconnect.Livraison.dto.*;
import com.esprit.campconnect.User.Entity.Utilisateur;

import java.util.List;

public interface ILivraisonService {

    LivraisonResponse createLivraison(LivraisonCreateRequest request);

    LivraisonResponse assignLivreur(Long idLivraison, Long livreurId);

    LivraisonResponse updateStatus(Long idLivraison, LivraisonStatusUpdateRequest request);

    List<LivraisonResponse> getMyLivraisons();

    List<LivraisonResponse> getAll();

    LivraisonStatsResponse getMyStats();

    List<AvailableOrderForLivraisonResponse> getAvailableClassicOrders();

    List<AvailableOrderForLivraisonResponse> getAvailableRepasOrders();

    List<Utilisateur> getLivreurs();
}