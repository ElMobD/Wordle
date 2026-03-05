package com.wordle.backend.service;

import com.wordle.backend.model.Game;
import com.wordle.backend.model.Guess;
import com.wordle.backend.model.User;
import com.wordle.backend.repository.GameRepository;
import com.wordle.backend.repository.GuessRepository;
import com.wordle.backend.repository.UserRepository;
import com.wordle.backend.model.Session;
import com.wordle.backend.repository.SessionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Transactional
public class GameService {

    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GuessRepository guessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WordService wordService;


    // Création d'une partie solo
    public Game createGameSolo(Long userId, Game.GameType gameType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));

        if (gameType == Game.GameType.DAILY) {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);
            Optional<Game> dailyToday = gameRepository.findByUserIdAndGameTypeAndCreatedAtBetween(
                    userId,
                    gameType,
                    startOfDay,
                    endOfDay
            );
            if (dailyToday.isPresent()) {
                return dailyToday.get();
            }
        }
        String answer = (gameType == Game.GameType.DAILY)
                ? wordService.getDailyWord()
                : wordService.getRandomWord();
        Game game = new Game(user, gameType, answer);
        return gameRepository.save(game);
    }
        // Récupérer une partie multi par session et round
    public Optional<Game> getGameBySessionAndRound(UUID sessionId, Integer roundNumber) {
        return gameRepository.findBySessionIdAndRoundNumber(sessionId, roundNumber);
    }
    // Création d'une partie multi
    public Game createGameMulti(UUID sessionId, Integer roundNumber, String answer) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée"));
        Game game = new Game(session, roundNumber, answer);
        return gameRepository.save(game);
    }
    
    // Récupérer la Game d'un joueur spécifique dans une session/round
    public Optional<Game> getGameBySessionAndUser(UUID sessionId, Integer roundNumber, Long userId) {
        return gameRepository.findBySessionIdAndRoundNumberAndUserId(sessionId, roundNumber, userId);
    }


    public Game submitGuess(UUID gameId, Long userId, String word) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Partie non trouvee"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));

        // Vérification d'accès solo/multi
        if (game.getUser() != null) {
            // Solo
            if (!game.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Acces refuse");
            }
        } else {
            // Multi : vérifier que le user fait partie de la session
            if (game.getSession() == null) {
                throw new IllegalArgumentException("Session manquante pour une partie multi");
            }
            // TODO : vérifier l'appartenance à la session (à faire dans SessionService ou ici)
        }

        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Partie terminee");
        }

        if (word == null || word.trim().isEmpty()) {
            throw new IllegalArgumentException("Mot invalide");
        }

        String guessWord = word.trim().toLowerCase();
        String answer = game.getAnswer().toLowerCase();

        if (guessWord.length() != answer.length()) {
            throw new IllegalArgumentException("Longueur de mot invalide");
        }

        if (!wordService.isValidWord(guessWord)) {
            throw new IllegalArgumentException("Mot non autorise");
        }

        String mask = buildResultMask(guessWord, answer);

        Guess guess = new Guess(game, user, game.getAttemptsUsed() + 1, guessWord, mask);
        guessRepository.save(guess);

        game.setAttemptsUsed(game.getAttemptsUsed() + 1);

        if (guessWord.equals(answer)) {
            game.setStatus(Game.GameStatus.WON);
            game.setCompletedAt(LocalDateTime.now());
        } else if (game.getAttemptsUsed() >= game.getMaxAttempts()) {
            game.setStatus(Game.GameStatus.LOST);
            game.setCompletedAt(LocalDateTime.now());
        }

        return gameRepository.save(game);
    }

    public Game getGame(UUID gameId, Long userId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Partie non trouvee"));

        if (!game.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Acces refuse");
        }

        return game;
    }

    public List<Game> getUserGames(Long userId) {
        return gameRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private String buildResultMask(String guess, String answer) {
        int length = guess.length();
        char[] mask = new char[length];
        boolean[] used = new boolean[length];

        for (int i = 0; i < length; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                mask[i] = '2';
                used[i] = true;
            } else {
                mask[i] = '0';
            }
        }

        for (int i = 0; i < length; i++) {
            if (mask[i] != '0') {
                continue;
            }
            for (int j = 0; j < length; j++) {
                if (!used[j] && guess.charAt(i) == answer.charAt(j)) {
                    mask[i] = '1';
                    used[j] = true;
                    break;
                }
            }
        }

        return new String(mask);
    }
}
