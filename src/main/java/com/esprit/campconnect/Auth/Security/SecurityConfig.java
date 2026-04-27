package com.esprit.campconnect.Auth.Security;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor

@EnableMethodSecurity

public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(
                                "/user/**",
                                "/auth/login",
                                "/auth/login/verify-2fa",
                                "/auth/register",
                                "/auth/google",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/config/**",
                                "/api/config/**",
                                "/events/**",
                                "/site-camping/**",
                                "/site-camping-avis/**",
                                "/inscriptionsite/**",
                                "/reclamations/**",
                        "/reclamation-notifications/**",
                                "/repas/**",
                                "/commandes-repas/**",
                                "/produits/**",
                                "/detail-panier/**",
                                "/paniers/**",
                                "/commandes/**",
                                "/details-commandes/**",
                                 "/commentaires",
                                "/uploads/**",
                                "/forums/**").permitAll()



                        .requestMatchers(HttpMethod.POST, "/stripe/webhook").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // ===============================
                        // ASSURANCE - PUBLIC
                        // ===============================
                        .requestMatchers(HttpMethod.GET, "/api/assurance/all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/assurance/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/garantie/**").permitAll()

                        // ===============================
                        // ASSURANCE - ADMIN ONLY
                        // ===============================
                        .requestMatchers(HttpMethod.POST, "/api/assurance/add").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/assurance/update").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/assurance/delete/*").hasRole("ADMINISTRATEUR")

                        // ===============================
                        // SOUSCRIPTIONS
                        // ===============================
                        .requestMatchers(HttpMethod.GET, "/api/souscription-assurance/all")
                        .hasAnyRole("ADMINISTRATEUR", "AGENT_ASSURANCE")

                        .requestMatchers(HttpMethod.GET, "/api/souscription-assurance/user/*")
                        .hasAnyRole("CLIENT", "ADMINISTRATEUR", "AGENT_ASSURANCE")

                        .requestMatchers(HttpMethod.POST, "/api/souscription-assurance/add/*/*")
                        .hasRole("CLIENT")

                        .requestMatchers(HttpMethod.PUT, "/api/souscription-assurance/update")
                        .hasAnyRole("CLIENT", "ADMINISTRATEUR")

                        .requestMatchers(HttpMethod.DELETE, "/api/souscription-assurance/delete/*")
                        .hasRole("ADMINISTRATEUR")

                        // ===============================
                        // SINISTRES
                        // ===============================
                        .requestMatchers(HttpMethod.GET, "/api/sinistre/all")
                        .hasAnyRole("ADMINISTRATEUR", "AGENT_ASSURANCE")

                        .requestMatchers(HttpMethod.GET, "/api/sinistre/souscription/*")
                        .hasAnyRole("CLIENT", "ADMINISTRATEUR", "AGENT_ASSURANCE")

                        .requestMatchers(HttpMethod.POST, "/api/sinistre/add/*")
                        .hasRole("CLIENT")

                        .requestMatchers(HttpMethod.PUT, "/api/sinistre/update")
                        .hasAnyRole("ADMINISTRATEUR", "AGENT_ASSURANCE")

                        .requestMatchers(HttpMethod.DELETE, "/api/sinistre/delete/*")
                        .hasRole("ADMINISTRATEUR")

                        .requestMatchers(HttpMethod.GET, "/api/document-assurance/sinistre/*")
                        .hasAnyRole("CLIENT", "ADMINISTRATEUR", "AGENT_ASSURANCE")

                        .requestMatchers(HttpMethod.POST, "/api/document-assurance/add/*")
                        .hasRole("CLIENT")

                        .requestMatchers(HttpMethod.DELETE, "/api/document-assurance/delete/*")
                        .hasAnyRole("CLIENT", "ADMINISTRATEUR", "AGENT_ASSURANCE")

                        // ===============================
                        // REMBOURSEMENTS
                        // ===============================
                        .requestMatchers(HttpMethod.GET, "/api/remboursement/all")
                        .hasAnyRole("ADMINISTRATEUR", "AGENT_ASSURANCE")

                        .requestMatchers(HttpMethod.GET, "/api/remboursement/*")
                        .hasAnyRole("ADMINISTRATEUR", "AGENT_ASSURANCE")

                        .requestMatchers(HttpMethod.POST, "/api/remboursement/add/*")
                        .hasAnyRole("ADMINISTRATEUR", "AGENT_ASSURANCE")

                        .requestMatchers(HttpMethod.PUT, "/api/remboursement/update")
                        .hasAnyRole("ADMINISTRATEUR", "AGENT_ASSURANCE")

                        .requestMatchers(HttpMethod.DELETE, "/api/remboursement/delete/*")
                        .hasRole("ADMINISTRATEUR")



                        .requestMatchers(HttpMethod.GET, "/publications/forum/**").permitAll()

                        // ===============================
                        // ADMIN AREA
                        // ===============================
                        .requestMatchers("/admin/**").hasRole("ADMINISTRATEUR")

                        // ===============================
                        // AUTHENTICATED ROUTES
                        // ===============================
                        .requestMatchers("/auth/2fa/**").authenticated()

                        .anyRequest().authenticated())


                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}