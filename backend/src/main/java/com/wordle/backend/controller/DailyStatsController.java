package com.wordle.backend.controller;

import com.wordle.backend.model.DailyStats;
import com.wordle.backend.model.User;
import com.wordle.backend.service.DailyStatsService;
import com.wordle.backend.service.JwtValidator;
import com.wordle.backend.service.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/stats")
public class DailyStatsController {

    @Autowired
    private JwtValidator jwtValidator;

    @Autowired
    private UserService userService;

    @Autowired
    private DailyStatsService dailyStatsService;

    /**
     * Endpoint pour récupérer les statistiques daily wordle du user authentifié
     */
    @GetMapping("/daily")
    public ResponseEntity<DailyStats> getDailyStats(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        // Extraire le token JWT depuis les détails de l'authentification
        String token = (String) authentication.getDetails();
        if (token == null) {
            return ResponseEntity.status(401).build();
        }

        // Décoder le token pour extraire l'email
        Claims claims = jwtValidator.validateToken(token);
        String email = claims.get("email", String.class);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }

        // Récupérer le user depuis l'email
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        Long userId = userOpt.get().getId();

        // Récupérer ou créer les stats daily pour ce user
        DailyStats stats = dailyStatsService.getOrCreateStats(userId);

        return ResponseEntity.ok(stats);
    }
}
