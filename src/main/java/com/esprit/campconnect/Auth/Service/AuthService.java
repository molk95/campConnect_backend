package com.esprit.campconnect.Auth.Service;

import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import com.esprit.campconnect.User.Repository.UtilisateurRepository;
import com.esprit.campconnect.Auth.DTO.AuthResponse;
import com.esprit.campconnect.Auth.DTO.LoginRequest;
import com.esprit.campconnect.Auth.DTO.RegisterRequest;
import com.esprit.campconnect.Auth.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class AuthService {
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(null, "Email déjà utilisé", null);
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));

        // Inscription publique : par défaut CLIENT
        utilisateur.setRole(request.getRole() != null ? request.getRole() : Role.CLIENT);

        Utilisateur savedUser = utilisateurRepository.save(utilisateur);
        String jwtToken = jwtService.generateToken(savedUser);

        return new AuthResponse(jwtToken, "Inscription réussie", savedUser.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getMotDePasse()
                )
        );

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String jwtToken = jwtService.generateToken(utilisateur);

        return new AuthResponse(jwtToken, "Connexion réussie", utilisateur.getRole().name());
    }
}
