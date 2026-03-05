package com.wordle.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import com.wordle.backend.application.LobbyService;
import com.wordle.backend.model.*;
import com.wordle.backend.repository.SessionGamePlayerRepository;
import com.wordle.backend.repository.SessionPlayerRepository;
import com.wordle.backend.websocket.response.WebSocketResponseFactory;
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
    private static final Logger logger = Logger.getLogger(GameStartService.class.getName());

    public GameStartService(LobbyService lobbyService,
                            WordService wordService,
                            GameService gameService,
                            SessionService sessionService,
                            WebSocketResponseFactory responseFactory,
                            SessionGamePlayerRepository sessionGamePlayerRepository,
                            SessionPlayerRepository sessionPlayerRepository) {
        this.lobbyService = lobbyService;
        this.wordService = wordService;
        this.gameService = gameService;
        this.sessionService = sessionService;
        this.sessionGamePlayerRepository = sessionGamePlayerRepository;
        this.sessionPlayerRepository = sessionPlayerRepository;
    }

    @Transactional
    public Game startGameTransactional(String sessionCode, User user) throws Exception {
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

        // 2. Créer la Game du round 1
        int roundNumber = 1;
        String answer = wordService.getRandomWord();
        Game game = gameService.createGameMulti(s.getId(), roundNumber, answer);
        logger.info("Game créée: " + game.getId());

        // 2b. Mettre à jour le currentGameId de la session
        s.setCurrentGameId(game.getId());
        Session savedSession = sessionService.save(s);
        logger.info("Game créée: " + game.getId() + ", currentGameId set to: " + savedSession.getCurrentGameId());

        // 2c. Créer les enregistrements session_game_player pour chaque joueur
        List<SessionPlayer> sessionPlayers = sessionPlayerRepository.findBySessionId(s.getId());
        logger.info("Nombre de joueurs: " + sessionPlayers.size());
        for (SessionPlayer sp : sessionPlayers) {
            SessionGamePlayer sgp = new SessionGamePlayer(s.getId(), game.getId(), sp.getUser().getId());
            sessionGamePlayerRepository.save(sgp);
            logger.info("SessionGamePlayer créé pour userId=" + sp.getUser().getId());
        }

        // Force initialization of lazy host relationship while transaction is active
        Hibernate.initialize(savedSession.getHost());
        logger.info("Host initialized");
        
        return game;
    }
    
    public Session getSessionByCode(String code) throws Exception {
        return lobbyService.getSessionByCode(code);
    }
}
