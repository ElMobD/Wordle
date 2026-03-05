package com.wordle.backend.controller;

import com.wordle.backend.model.Game;
import com.wordle.backend.model.Guess;
import com.wordle.backend.model.User;
import com.wordle.backend.repository.GuessRepository;
import com.wordle.backend.repository.UserRepository;
import com.wordle.backend.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private GuessRepository guessRepository;

    @Autowired
    private UserRepository userRepository;


    // Créer une partie solo (DAILY ou RANDOM)
    @PostMapping
    public ResponseEntity<?> createGame(@RequestBody Map<String, String> body, Authentication authentication) {
        try {
            Long userId = getUserId(authentication);
            String gameTypeValue = body.get("gameType");
            if (gameTypeValue == null || gameTypeValue.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "gameType requis"));
            }
            Game.GameType gameType = Game.GameType.valueOf(gameTypeValue.trim().toUpperCase());
            Game game = gameService.createGameSolo(userId, gameType);
            return ResponseEntity.ok(buildGameResponse(game, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Créer une partie multi (SESSION)
    @PostMapping("/multiplayer")
    public ResponseEntity<?> createGameMulti(@RequestBody Map<String, Object> body, Authentication authentication) {
        try {
            String sessionIdStr = (String) body.get("sessionId");
            Integer roundNumber = (Integer) body.get("roundNumber");
            String answer = (String) body.get("answer");
            if (sessionIdStr == null || roundNumber == null || answer == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "sessionId, roundNumber et answer requis"));
            }
            UUID sessionId = UUID.fromString(sessionIdStr);
            Game game = gameService.createGameMulti(sessionId, roundNumber, answer);
            // On peut retourner la game sans guesses ici
            return ResponseEntity.ok(buildGameResponse(game, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


    // Récupérer une partie solo
    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGame(@PathVariable UUID gameId, Authentication authentication) {
        try {
            Long userId = getUserId(authentication);
            Game game = gameService.getGame(gameId, userId);
            return ResponseEntity.ok(buildGameResponse(game, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Récupérer une partie multi par session/round
    @GetMapping("/multiplayer")
    public ResponseEntity<?> getGameMulti(@RequestParam String sessionId, @RequestParam Integer roundNumber, Authentication authentication) {
        try {
            Long userId = getUserId(authentication);
            UUID sessionUUID = UUID.fromString(sessionId);
            Game game = gameService.getGameBySessionAndRound(sessionUUID, roundNumber)
                    .orElseThrow(() -> new RuntimeException("Partie non trouvée pour cette session/round"));
            return ResponseEntity.ok(buildGameResponse(game, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


    // Soumettre un mot (solo ou multi)
    @PostMapping("/{gameId}/guess")
    public ResponseEntity<?> submitGuess(
            @PathVariable UUID gameId,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        try {
            Long userId = getUserId(authentication);
            String word = body.get("word");
            if (word == null || word.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "word requis"));
            }

            Game game = gameService.submitGuess(gameId, userId, word);
            return ResponseEntity.ok(buildGameResponse(game, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> buildGameResponse(Game game, Long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", game.getId());
        response.put("gameType", game.getGameType());
        response.put("maxAttempts", game.getMaxAttempts());
        response.put("attemptsUsed", game.getAttemptsUsed());
        response.put("status", game.getStatus());
        response.put("createdAt", game.getCreatedAt());
        response.put("completedAt", game.getCompletedAt());

        List<Guess> guesses;
        if (userId != null) {
            // Solo ou guesses du joueur courant en multi
            guesses = guessRepository.findByGameIdAndUserIdOrderByAttemptNo(game.getId(), userId);
        } else {
            // Toutes les guesses (cas rare, ex: admin)
            guesses = guessRepository.findByGameIdOrderByAttemptNo(game.getId());
        }
        List<Map<String, Object>> simplifiedGuesses = new java.util.ArrayList<>();
        for (Guess guess : guesses) {
            Map<String, Object> guessMap = new HashMap<>();
            guessMap.put("attemptNo", guess.getAttemptNo());
            guessMap.put("word", guess.getGuess());
            guessMap.put("mask", guess.getResultMask());
            simplifiedGuesses.add(guessMap);
        }
        response.put("guesses", simplifiedGuesses);

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
