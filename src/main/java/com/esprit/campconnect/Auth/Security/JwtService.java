package com.esprit.campconnect.Auth.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    private static final long JWT_EXPIRATION = 1000L * 60 * 60 * 24;
    private static final long TEMP_2FA_EXPIRATION = 1000L * 60 * 5;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        String role = authorities.stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_CLIENT");

        claims.put("role", role);
        claims.put("tokenType", "ACCESS");

        if (userDetails instanceof com.esprit.campconnect.User.Entity.Utilisateur utilisateur) {
            Long userId = utilisateur.getId();
            if (userId != null) {
                claims.put("userId", userId);
            }
        }

        return generateToken(claims, userDetails, jwtExpiration);
    }

    public String generateTemp2FAToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        String role = authorities.stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_CLIENT");

        claims.put("role", role);
        claims.put("tokenType", "TEMP_2FA");

        if (userDetails instanceof com.esprit.campconnect.User.Entity.Utilisateur utilisateur) {
            Long userId = utilisateur.getId();
            if (userId != null) {
                claims.put("userId", userId);
            }
        }

        return generateToken(claims, userDetails, TEMP_2FA_EXPIRATION);
    }

    public void addUserIdToClaims(Map<String, Object> claims, Long userId) {
        claims.put("userId", userId);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return generateToken(extraClaims, userDetails, jwtExpiration);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTemp2FATokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        final String tokenType = extractTokenType(token);

        return username.equals(userDetails.getUsername())
                && "TEMP_2FA".equals(tokenType)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}