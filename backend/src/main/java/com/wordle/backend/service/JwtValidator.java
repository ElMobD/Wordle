package com.wordle.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class JwtValidator {

    @Value("${jwt.secret.key}")
    private String secretKey;

    /**
     * Valide le JWT et retourne les claims si valide
     * @param token JWT token à valider
     * @return Claims du token si valide
     * @throws JwtException si le token est invalide ou expiré
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token expiré", e);
        } catch (JwtException e) {
            throw new JwtException("Token invalide: " + e.getMessage(), e);
        }
    }

    /**
     * Extrait l'email du JWT token
     * @param token JWT token
     * @return Email du token
     */
    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject(); // Subject contient l'email
    }
}
