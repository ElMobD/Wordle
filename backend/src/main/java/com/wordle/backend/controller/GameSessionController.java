package com.wordle.backend.controller;

import com.wordle.backend.model.Game;
import com.wordle.backend.model.GameSession;
import com.wordle.backend.model.GameSessionMember;
import com.wordle.backend.model.GameSessionRound;
import com.wordle.backend.model.GameSessionScore;
import com.wordle.backend.model.User;
import com.wordle.backend.repository.GameRepository;
import com.wordle.backend.repository.GameSessionMemberRepository;
import com.wordle.backend.repository.GameSessionRepository;
import com.wordle.backend.repository.GameSessionRoundRepository;
import com.wordle.backend.repository.GameSessionScoreRepository;
import com.wordle.backend.repository.UserRepository;
import com.wordle.backend.service.GameSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions")
public class GameSessionController {

    @Autowired
    private GameSessionService gameSessionService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameSessionMemberRepository memberRepository;

    @Autowired
    private GameSessionScoreRepository scoreRepository;

    @Autowired
    private GameSessionRoundRepository roundRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    // Créer une session
    @PostMapping
    public ResponseEntity<?> createSession(@RequestBody Map<String, Object> body, Authentication authentication) {
        try {
            Long userId = getUserId(authentication);
            int wordLength = (int) body.getOrDefault("wordLength", 5);
            int maxAttempts = (int) body.getOrDefault("maxAttempts", 6);
            int roundTimeSeconds = (int) body.getOrDefault("roundTimeSeconds", 120);
            int totalRounds = (int) body.getOrDefault("totalRounds", 1);

            GameSession session = gameSessionService.createSession(userId, wordLength, maxAttempts, roundTimeSeconds, totalRounds);
            return ResponseEntity.ok(buildSessionResponse(session));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Rejoindre une session par code
    @PostMapping("/join")
    public ResponseEntity<?> joinSession(@RequestBody Map<String, String> body, Authentication authentication) {
        try {
            Long userId = getUserId(authentication);
            String code = body.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Code requis"));
            }

            GameSession session = gameSessionService.joinSession(code.trim().toUpperCase(), userId);
            return ResponseEntity.ok(buildSessionResponse(session));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Récupérer une session par code
    @GetMapping("/{code}")
    public ResponseEntity<?> getSession(@PathVariable String code, Authentication authentication) {
        try {
            getUserId(authentication);
            GameSession session = gameSessionRepository.findByCode(code.toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Session non trouvee"));
            return ResponseEntity.ok(buildSessionResponse(session));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Démarrer le prochain round
    @PostMapping("/{code}/start-round")
    public ResponseEntity<?> startNextRound(@PathVariable String code, Authentication authentication) {
        try {
            Long userId = getUserId(authentication);
            GameSessionRound round = gameSessionService.startNextRound(code.toUpperCase(), userId);
            return ResponseEntity.ok(buildRoundResponse(round));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Terminer le round actuel
    @PostMapping("/{code}/end-round")
    public ResponseEntity<?> endCurrentRound(@PathVariable String code, Authentication authentication) {
        try {
            Long userId = getUserId(authentication);
            GameSessionRound round = gameSessionService.endCurrentRound(code.toUpperCase(), userId);
            return ResponseEntity.ok(buildRoundResponse(round));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Liste des membres de la session
    @GetMapping("/{code}/members")
    public ResponseEntity<?> getMembers(@PathVariable String code, Authentication authentication) {
        try {
            getUserId(authentication);
            GameSession session = gameSessionRepository.findByCode(code.toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Session non trouvee"));

            List<GameSessionMember> members = memberRepository.findBySessionId(session.getId());
            List<Map<String, Object>> membersData = members.stream().map(m -> {
                Map<String, Object> data = new HashMap<>();
                data.put("userId", m.getUser().getId());
                data.put("name", m.getUser().getName());
                data.put("picture", m.getUser().getPicture());
                data.put("email", m.getUser().getEmail());
                data.put("joinedAt", m.getJoinedAt());
                return data;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(membersData);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Classement de la session
    @GetMapping("/{code}/leaderboard")
    public ResponseEntity<?> getLeaderboard(@PathVariable String code, Authentication authentication) {
        try {
            getUserId(authentication);
            GameSession session = gameSessionRepository.findByCode(code.toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Session non trouvee"));

            List<GameSessionScore> scores = scoreRepository.findBySessionId(session.getId());
            List<Map<String, Object>> leaderboard = scores.stream()
                    .sorted(Comparator.comparingInt(GameSessionScore::getTotalScore).reversed())
                    .map(s -> {
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("userId", s.getUser().getId());
                        entry.put("name", s.getUser().getName());
                        entry.put("picture", s.getUser().getPicture());
                        entry.put("totalScore", s.getTotalScore());
                        return entry;
                    }).collect(Collectors.toList());

            return ResponseEntity.ok(leaderboard);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Récupérer la partie du joueur pour le round actuel
    @GetMapping("/{code}/my-game")
    public ResponseEntity<?> getMyGame(@PathVariable String code, Authentication authentication) {
        try {
            Long userId = getUserId(authentication);
            GameSession session = gameSessionRepository.findByCode(code.toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Session non trouvee"));

            if (session.getStatus() != GameSession.GameSessionStatus.IN_PROGRESS) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun round en cours"));
            }

            int currentRound = session.getCurrentRound();
            GameSessionRound round = roundRepository.findBySessionIdAndRoundIndex(session.getId(), currentRound)
                    .orElseThrow(() -> new RuntimeException("Round actuel introuvable"));

            Optional<Game> gameOpt = gameRepository.findBySessionRoundIdAndUserId(round.getId(), userId);
            if (gameOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Partie non trouvee"));
            }

            Game game = gameOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("gameId", game.getId());
            response.put("status", game.getStatus());
            response.put("attemptsUsed", game.getAttemptsUsed());
            response.put("maxAttempts", game.getMaxAttempts());
            response.put("roundIndex", round.getRoundIndex());
            response.put("roundStartedAt", round.getStartedAt());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> buildSessionResponse(GameSession session) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", session.getId());
        response.put("code", session.getCode());
        response.put("hostId", session.getHost().getId());
        response.put("status", session.getStatus());
        response.put("wordLength", session.getWordLength());
        response.put("maxAttempts", session.getMaxAttempts());
        response.put("roundTimeSeconds", session.getRoundTimeSeconds());
        response.put("totalRounds", session.getTotalRounds());
        response.put("currentRound", session.getCurrentRound());
        response.put("createdAt", session.getCreatedAt());
        return response;
    }

    private Map<String, Object> buildRoundResponse(GameSessionRound round) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", round.getId());
        response.put("roundIndex", round.getRoundIndex());
        response.put("status", round.getStatus());
        response.put("startedAt", round.getStartedAt());
        response.put("endedAt", round.getEndedAt());
        return response;
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Non authentifie");
        }
        String email = (String) authentication.getPrincipal();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));
        return user.getId();
    }
}
