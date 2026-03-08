package com.wordle.backend.controller;

import com.wordle.backend.service.JwtValidator;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wordle.backend.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private JwtValidator jwtValidator;
    @Autowired
    private UserService userService;

    /**
     * Endpoint sécurisé pour récupérer le profil du user authentifié
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        // Le principal contient l'email (String)
        //String email = (String) authentication.getPrincipal();
        
        // Le token JWT est stocké dans les détails
        String token = (String) authentication.getDetails();
        
        // Décoder le token pour extraire toutes les informations
        Claims claims = jwtValidator.validateToken(token);
        String email = claims.get("email", String.class);

        if (email == null || userService.findByEmail(email).isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("email", email);
        profile.put("name", claims.get("name", String.class));
        profile.put("picture", claims.get("picture", String.class));
        profile.put("locale", claims.get("locale", String.class));
        profile.put("sub", claims.get("sub", String.class)); // Google ID
        profile.put("authenticated", true);

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuthentication(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication == null) {
            response.put("authenticated", false);
            return ResponseEntity.status(401).body(response);
        }

        Object details = authentication.getDetails();
        if (!(details instanceof String token)) {
            response.put("authenticated", false);
            return ResponseEntity.status(401).body(response);
        }

        try {
            Claims claims = jwtValidator.validateToken(token);
            String email = claims.get("email", String.class);
            if (email == null) {
                response.put("authenticated", false);
                return ResponseEntity.status(401).body(response);
            }

            com.wordle.backend.model.User existingUser = userService.findByEmail(email).orElse(null);
            if (existingUser == null) {
                response.put("authenticated", false);
                return ResponseEntity.status(401).body(response);
            }

            response.put("authenticated", true);
            response.put("user", authentication.getPrincipal());
            response.put("userId", existingUser.getId());
        } catch (Exception e) {
            response.put("authenticated", false);
            return ResponseEntity.status(401).body(response);
        }

        return ResponseEntity.ok(response);
    }
}
