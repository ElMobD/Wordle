package com.wordle.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordle.backend.service.JwtValidator;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // 1️⃣ Cherche le token dans le header Authorization
            String tokenJwt = extractJwtFromRequest(request);

            // 2️⃣ S'il existe
            if (StringUtils.hasText(tokenJwt)) {
                try {
                    // 3️⃣ Valide le token
                    String email = jwtValidator.getEmailFromToken(tokenJwt);
                    
                    // 4️⃣ Crée une authentification avec l'email
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(email, null, null);
                    
                    // 4.5️⃣ Stocke le token JWT dans les détails pour pouvoir le récupérer plus tard
                    authentication.setDetails(tokenJwt);
                    
                    // 5️⃣ Dit à Spring: "l'utilisateur est authentifié"
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (JwtException e) {
                    // ❌ Token invalide/expiré
                    sendUnauthorizedError(response, "Invalid or expired token");
                    return;  // STOP ici, ne continue pas
                }
            }

            // 6️⃣ Laisse la requête continuer (vers le contrôleur)
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendUnauthorizedError(response, "Authentication error: " + e.getMessage());
        }
    }

    /**
     * Extrait le JWT du header Authorization
     * Format attendu: "Bearer <token>"
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Enlève "Bearer " et récupère juste le token
        }
        return null;
    }

    /**
     * Envoie une réponse 401 avec message d'erreur JSON
     */
    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, String> error = new HashMap<>();
        error.put("error", "Unauthorized");
        error.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
