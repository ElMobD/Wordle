package com.wordle.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import com.wordle.backend.service.SessionService;
import com.wordle.backend.model.PlayerScore;
import java.util.Map;
import java.util.List;
import com.wordle.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/current")
    public Map<String, Object> getCurrentSession(Authentication authentication) {
        System.out.println("Requête GET /api/session/current reçue avec authentication: " + authentication);
        if (authentication == null) {
            System.out.println("Aucun utilisateur authentifié V1");
            return Map.of();
        }
        Long userId = getUserId(authentication);
        if (userId == null) {
            return Map.of();
        }
        String sessionCode = sessionService.getSessionCodeForUserId(userId);
        if (sessionCode != null) {
            return Map.of("sessionCode", sessionCode);
        } else {
            return Map.of();
        }
    }

    @GetMapping("/{sessionCode}/leaderboard")
    public List<PlayerScore> getLeaderboard(@PathVariable String sessionCode) {
        System.out.println("Requête GET /api/session/" + sessionCode + "/leaderboard reçue");
        return sessionService.getLeaderboard(sessionCode);
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        String email = (String) authentication.getPrincipal();
        com.wordle.backend.model.User user = userRepository.findByEmail(email)
                .orElse(null);
        return user != null ? user.getId() : null;
    }
}
