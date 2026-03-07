package com.wordle.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import com.wordle.backend.application.LobbyService;
import com.wordle.backend.model.*;
import com.wordle.backend.repository.SessionGamePlayerRepository;
import com.wordle.backend.repository.SessionPlayerRepository;
import com.wordle.backend.repository.GameRepository;
import com.wordle.backend.websocket.response.WebSocketResponseFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class GameStartService {
    private final LobbyService lobbyService;
    private final WordService wordService;
    private final GameService gameService;
    private final SessionService sessionService;
    private final SessionGamePlayerRepository sessionGamePlayerRepository;
    private final SessionPlayerRepository sessionPlayerRepository;
    private final GameRepository gameRepository;
    private static final Logger logger = Logger.getLogger(GameStartService.class.getName());

    public GameStartService(LobbyService lobbyService,
                            WordService wordService,
                            GameService gameService,
                            SessionService sessionService,
                            WebSocketResponseFactory responseFactory,
                            SessionGamePlayerRepository sessionGamePlayerRepository,
                            SessionPlayerRepository sessionPlayerRepository,
                            GameRepository gameRepository) {
        this.lobbyService = lobbyService;
        this.wordService = wordService;
        this.gameService = gameService;
        this.sessionService = sessionService;
        this.sessionGamePlayerRepository = sessionGamePlayerRepository;
        this.sessionPlayerRepository = sessionPlayerRepository;
        this.gameRepository = gameRepository;
    }

    @Transactional
    public List<Game> startGameTransactional(String sessionCode, User user) throws Exception {
        Session s = lobbyService.getSessionByCode(sessionCode);
        logger.info("Session trouvée: " + s.getId() + ", status=" + s.getStatus() + ", hostId=" + s.getHost().getId() + ", userId=" + user.getId());
        
        if (!s.getHost().getId().equals(user.getId())) {
            logger.warning("Pas l'hôte: hostId=" + s.getHost().getId() + ", userId=" + user.getId());
            throw new IllegalArgumentException("Seul l'hôte peut démarrer la partie.");
        }
        
        if (!"LOBBY".equals(s.getStatus())) {
            logger.warning("Status incorrect: " + s.getStatus());
            throw new IllegalArgumentException("La partie est déjà démarrée ou terminée.");
        }
        
        logger.info("Démarrage de la partie pour session " + sessionCode);
        
        // 1. Changer le statut
        s.setStatus("IN_PROGRESS");
        s.setUpdatedAt(java.time.LocalDateTime.now());
        sessionService.save(s);

        return createRoundGames(s, 1);
    }

    @Transactional
    public List<Game> startNextRoundTransactional(String sessionCode, User user) throws Exception {
        Session s = lobbyService.getSessionByCode(sessionCode);
        logger.info("NEXT_ROUND demandé pour session " + s.getId() + ", hostId=" + s.getHost().getId() + ", userId=" + user.getId());

        if (!s.getHost().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Seul l'hôte peut lancer le prochain round.");
        }

        if (!"IN_PROGRESS".equals(s.getStatus())) {
            throw new IllegalArgumentException("La session n'est pas en cours.");
        }

        Integer currentRound = s.getCurrentRound();
        if (currentRound == null || currentRound <= 0) {
            throw new IllegalArgumentException("Aucun round actif.");
        }

        if (!gameService.isRoundFinished(s, currentRound)) {
            throw new IllegalArgumentException("Le round n'est pas terminé (timer ou joueurs).");
        }

        // Finaliser les scores du round qui vient de se terminer
        gameService.finalizeRoundScores(s.getId(), currentRound, s.getTimeLimit());
        logger.info("Scores finalisés pour le round " + currentRound + " de la session " + s.getCode());

        if (currentRound >= s.getRounds()) {
            s.setStatus("FINISHED");
            s.setUpdatedAt(LocalDateTime.now());
            sessionService.save(s);
            logger.info("Session " + s.getCode() + " terminée après le round " + currentRound);
            return List.of();
        }

        int nextRound = currentRound + 1;
        return createRoundGames(s, nextRound);
    }

    private List<Game> createRoundGames(Session s, int roundNumber) {
        // 1. Récupérer les joueurs de la session
        List<SessionPlayer> sessionPlayers = sessionPlayerRepository.findBySessionId(s.getId());
        logger.info("Nombre de joueurs: " + sessionPlayers.size());

        // 2. Créer le mot pour ce round en respectant le wordLength de la session
        String answer = wordService.getRandomWord(s.getWordLength());
        LocalDateTime roundStartedAt = LocalDateTime.now();
        logger.info("Mot du round " + roundNumber + " (longueur " + s.getWordLength() + "): " + answer);

        // 3. Créer une Game par joueur avec le même mot
        List<Game> games = new ArrayList<>();
        for (SessionPlayer sp : sessionPlayers) {
            // Créer la game pour ce joueur
            Game game = gameService.createGameMulti(s.getId(), roundNumber, answer);
            game.setUser(sp.getUser()); // Associer la game au joueur
            game.setCreatedAt(roundStartedAt); // Même horodatage pour synchroniser le timer de tous les joueurs
            game = gameRepository.save(game); // Sauvegarder la game avec le user
            games.add(game);
            logger.info("Game créée pour userId=" + sp.getUser().getId() + ", gameId=" + game.getId());
            
            // Créer l'enregistrement session_game_player
            SessionGamePlayer sgp = new SessionGamePlayer(s.getId(), game.getId(), sp.getUser().getId());
            sessionGamePlayerRepository.save(sgp);
            logger.info("SessionGamePlayer créé pour userId=" + sp.getUser().getId());
        }

        // 4. Mettre à jour le currentRound de la session
        if (!games.isEmpty()) {
            s.setCurrentRound(roundNumber);
            s.setUpdatedAt(LocalDateTime.now());
            Session savedSession = sessionService.save(s);
            logger.info("CurrentRound set to: " + savedSession.getCurrentRound());
            
            // Force initialization of lazy host relationship while transaction is active
            Hibernate.initialize(savedSession.getHost());
            logger.info("Host initialized");
        }
        
        return games;
    }
    
    public Session getSessionByCode(String code) throws Exception {
        return lobbyService.getSessionByCode(code);
    }
}
