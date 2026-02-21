package com.wordle.backend.service;

import com.wordle.backend.model.Game;
import java.time.Duration;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class GameSessionService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameSessionMemberRepository memberRepository;

    @Autowired
    private GameSessionRoundRepository roundRepository;

    @Autowired
    private GameSessionScoreRepository scoreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private WordService wordService;

    private final Random random = new Random();

    public GameSession createSession(Long hostId, int wordLength, int maxAttempts, int roundTimeSeconds, int totalRounds) {
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));

        String code = generateUniqueCode();
        String firstWord = wordService.getRandomWordByLength(wordLength);

        GameSession session = new GameSession(code, host, firstWord);
        session.setWordLength(wordLength);
        session.setMaxAttempts(maxAttempts);
        session.setRoundTimeSeconds(roundTimeSeconds);
        session.setTotalRounds(totalRounds);
        session.setCurrentRound(0);
        session.setStatus(GameSession.GameSessionStatus.LOBBY);

        GameSession saved = gameSessionRepository.save(session);

        GameSessionMember hostMember = new GameSessionMember(saved, host);
        memberRepository.save(hostMember);

        ensureScoreEntry(saved, host);

        return saved;
    }

    public GameSession joinSession(String code, Long userId) {
        GameSession session = gameSessionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Session non trouvee"));

        if (session.getStatus() != GameSession.GameSessionStatus.LOBBY) {
            throw new IllegalArgumentException("La session a deja commence");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));

        Optional<GameSessionMember> existing = memberRepository.findBySessionIdAndUserId(session.getId(), userId);
        if (existing.isEmpty()) {
            memberRepository.save(new GameSessionMember(session, user));
        }

        ensureScoreEntry(session, user);

        return session;
    }

    public GameSessionRound startNextRound(String code, Long hostId) {
        GameSession session = gameSessionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Session non trouvee"));

        if (!session.getHost().getId().equals(hostId)) {
            throw new IllegalArgumentException("Seul l'hote peut demarrer un round");
        }

        if (session.getStatus() == GameSession.GameSessionStatus.ENDED) {
            throw new IllegalArgumentException("La session est terminee");
        }

        int nextRound = session.getCurrentRound() + 1;
        if (nextRound > session.getTotalRounds()) {
            throw new IllegalArgumentException("Tous les rounds sont termines");
        }

        Optional<GameSessionRound> existingRound = roundRepository.findBySessionIdAndRoundIndex(session.getId(), nextRound);
        if (existingRound.isPresent()) {
            throw new IllegalArgumentException("Ce round existe deja");
        }

        String word = wordService.getRandomWordByLength(session.getWordLength());

        GameSessionRound round = new GameSessionRound(session, nextRound, word);
        round.setStatus(GameSessionRound.RoundStatus.IN_PROGRESS);
        round.setStartedAt(LocalDateTime.now());
        GameSessionRound savedRound = roundRepository.save(round);

        session.setStatus(GameSession.GameSessionStatus.IN_PROGRESS);
        session.setCurrentRound(nextRound);
        session.setWord(word);
        gameSessionRepository.save(session);

        List<GameSessionMember> members = memberRepository.findBySessionId(session.getId());
        for (GameSessionMember member : members) {
            User user = member.getUser();
            if (gameRepository.findBySessionRoundIdAndUserId(savedRound.getId(), user.getId()).isPresent()) {
                continue;
            }
            Game game = new Game(user, Game.GameType.RANDOM, word);
            game.setMaxAttempts(session.getMaxAttempts());
            game.setAttemptsUsed(0);
            game.setStatus(Game.GameStatus.IN_PROGRESS);
            game.setSession(session);
            game.setSessionRound(savedRound);
            gameRepository.save(game);
            ensureScoreEntry(session, user);
        }

        return savedRound;
    }

    public GameSessionRound endCurrentRound(String code, Long hostId) {
        GameSession session = gameSessionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Session non trouvee"));

        if (!session.getHost().getId().equals(hostId)) {
            throw new IllegalArgumentException("Seul l'hote peut terminer un round");
        }

        if (session.getStatus() != GameSession.GameSessionStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Aucun round en cours");
        }

        int roundIndex = session.getCurrentRound();
        GameSessionRound round = roundRepository.findBySessionIdAndRoundIndex(session.getId(), roundIndex)
                .orElseThrow(() -> new RuntimeException("Round introuvable"));

        if (round.getStatus() != GameSessionRound.RoundStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Ce round n'est pas en cours");
        }

        LocalDateTime endedAt = LocalDateTime.now();
        round.setStatus(GameSessionRound.RoundStatus.ENDED);
        round.setEndedAt(endedAt);
        roundRepository.save(round);

        List<Game> games = gameRepository.findBySessionRoundId(round.getId());
        for (Game game : games) {
            if (game.getStatus() == Game.GameStatus.IN_PROGRESS) {
                game.setStatus(Game.GameStatus.LOST);
                game.setCompletedAt(endedAt);
                gameRepository.save(game);
            }

            int roundScore = calculateScore(session, round, game);
            if (roundScore > 0) {
                GameSessionScore score = scoreRepository.findBySessionIdAndUserId(session.getId(), game.getUser().getId())
                        .orElseGet(() -> new GameSessionScore(session, game.getUser()));
                int currentScore = score.getTotalScore() == null ? 0 : score.getTotalScore();
                score.setTotalScore(currentScore + roundScore);
                scoreRepository.save(score);
            }
        }

        if (roundIndex >= session.getTotalRounds()) {
            session.setStatus(GameSession.GameSessionStatus.ENDED);
        }
        gameSessionRepository.save(session);

        return round;
    }

    private void ensureScoreEntry(GameSession session, User user) {
        if (scoreRepository.findBySessionIdAndUserId(session.getId(), user.getId()).isEmpty()) {
            scoreRepository.save(new GameSessionScore(session, user));
        }
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String code = randomCode();
            if (gameSessionRepository.findByCode(code).isEmpty()) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de generer un code unique");
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return builder.toString();
    }

    private int calculateScore(GameSession session, GameSessionRound round, Game game) {
        if (game.getStatus() != Game.GameStatus.WON) {
            return 0;
        }

        if (round.getStartedAt() == null || game.getCompletedAt() == null) {
            return 0;
        }

        long elapsedSeconds = Duration.between(round.getStartedAt(), game.getCompletedAt()).getSeconds();
        int totalSeconds = session.getRoundTimeSeconds();
        long remainingSeconds = Math.max(0, totalSeconds - elapsedSeconds);
        double timeRatio = totalSeconds > 0 ? (double) remainingSeconds / totalSeconds : 0.0;

        int attemptsRemaining = Math.max(0, session.getMaxAttempts() - game.getAttemptsUsed());

        int timeScore = (int) Math.round(1000.0 * timeRatio);
        int attemptScore = 100 * attemptsRemaining;
        return timeScore + attemptScore;
    }
}
