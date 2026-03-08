package com.wordle.backend.websocket.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordle.backend.model.Session;
import com.wordle.backend.model.SessionPlayer;
import com.wordle.backend.model.User;
import com.wordle.backend.repository.SessionPlayerRepository;
import com.wordle.backend.service.SessionChatService;
import com.wordle.backend.websocket.LobbyWebSocketHandler;
import com.wordle.backend.model.SessionChat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class WebSocketResponseFactory {
    @Autowired
    private SessionPlayerRepository sessionPlayerRepository;
    @Autowired
    private SessionChatService sessionChatService;
    @Autowired
    private com.wordle.backend.service.GameService gameService;
    private final ObjectMapper mapper;
    private static final Logger logger = Logger.getLogger(LobbyWebSocketHandler.class.getName());
    public WebSocketResponseFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String sessionCreated(Session session) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "session_created");
        node.put("sessionCode", session.getCode());
        node.put("rounds", session.getRounds());
        node.put("timeLimit", session.getTimeLimit());
        node.put("wordLength", session.getWordLength());
        return node.toString();
    }

    public String playerJoined(User user, String sessionCode) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "player_joined");
        node.put("userId", user.getId());
        node.put("userName", user.getName());
        node.put("LobbyCode", sessionCode);
        return node.toString();
    }

    public String chat(User user, String message) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "chat");
        node.put("userId", user.getId());
        node.put("userName", user.getName());
        node.put("message", message);
        return node.toString();
    }

    public String error(String message) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "error");
        node.put("message", message);
        return node.toString();
    }

    public String errorWithSessionCode(String message, String sessionCode) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "error");
        node.put("message", message);
        if (sessionCode != null) node.put("sessionCode", sessionCode);
        return node.toString();
    }

    public String lobbyInfo(Session session) {
        Map<String, Object> info = new HashMap<>();
        info.put("type", "lobbyInfo");
        info.put("sessionCode", session.getCode());
        info.put("rounds", session.getRounds());
        info.put("timeLimit", session.getTimeLimit());
        info.put("wordLength", session.getWordLength());
        info.put("status", session.getStatus());
        info.put("host", session.getHost().getName());
        info.put("hostId", session.getHost().getId());
        logger.info("Session " + session.getCode() + " status: " + session.getStatus() + ", currentRound: " + session.getCurrentRound());
        info.put("currentRound", session.getCurrentRound());

        // Récupère les joueurs via le repository
        List<Map<String, Object>> players = new ArrayList<>();
        List<SessionPlayer> sessionPlayers = sessionPlayerRepository.findBySessionId(session.getId());
        for (SessionPlayer sp : sessionPlayers) {
            User user = sp.getUser();
            Map<String, Object> playerData = new HashMap<>();
            playerData.put("id", user.getId());
            playerData.put("name", user.getName());
            playerData.put("picture", user.getPicture());
            playerData.put("isHost", sp.isHost());
            playerData.put("joinedAt", sp.getJoinedAt());
            // Ajoute d'autres champs si besoin (email, etc.)
            players.add(playerData);
        }
        info.put("players", players);

        try {
            return mapper.writeValueAsString(info);
        } catch (Exception e) {
            return "{\"type\":\"lobbyInfo\",\"error\":\"Erreur de sérialisation\"}";
        }
    }
    public String playerLeft(User user, String sessionCode) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "player_left");
        node.put("userId", user.getId());
        node.put("userName", user.getName());
        node.put("sessionCode", sessionCode);
        return node.toString();
    }
    public String pong() {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "pong");
        return node.toString();
    }
    /**
     * Renvoie l'historique des messages du chat pour une session donnée
     */
    public String chatHistory(Session session) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "chat_history");
        node.put("sessionCode", session.getCode());
        // Utilise le fetch join pour charger les users avec les messages
        List<SessionChat> messages = sessionChatService.getMessagesBySessionWithUser(session.getId());
        var array = mapper.createArrayNode();
        for (SessionChat msg : messages) {
            User user = msg.getUser();
            String userName = user != null ? user.getName() : "Anonyme";
            Long userId = user != null ? user.getId() : null;
            ObjectNode msgNode = mapper.createObjectNode();
            msgNode.put("userId", userId);
            msgNode.put("userName", userName);
            msgNode.put("message", msg.getMessage());
            msgNode.put("sentAt", msg.getSentAt().toString());
            array.add(msgNode);
        }
        node.set("messages", array);
        return node.toString();
    }
    public String gameStart(com.wordle.backend.model.Game game) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "game_start");
        node.put("gameId", game.getId().toString());
        node.put("roundNumber", game.getRoundNumber());
        node.put("answerLength", game.getAnswer().length());
        node.put("maxAttempts", game.getMaxAttempts());
        node.put("status", game.getStatus().toString());
        node.put("sessionId", game.getSession().getId().toString());
        
        // Ajouter les settings de la session
        com.wordle.backend.model.Session session = game.getSession();
        if (session != null) {
            node.put("rounds", session.getRounds());
            node.put("timeLimit", session.getTimeLimit());
            node.put("wordLength", session.getWordLength());
            node.put("currentRound", session.getCurrentRound());
            node.put("hostId", session.getHost().getId());
            node.put("remainingTime", computeRemainingTime(game, session.getTimeLimit()));
            node.set("players_status", buildPlayersStatus(session, game.getRoundNumber()));
        }
        
        return node.toString();
    }
    
    public String loadGame(com.wordle.backend.model.Game game) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "load_game");
        node.put("gameId", game.getId().toString());
        node.put("roundNumber", game.getRoundNumber());
        node.put("answerLength", game.getAnswer().length());
        node.put("maxAttempts", game.getMaxAttempts());
        node.put("attemptsUsed", game.getAttemptsUsed());
        node.put("status", game.getStatus().toString());
        node.put("sessionId", game.getSession().getId().toString());
        
        // Ajouter les settings de la session
        com.wordle.backend.model.Session session = game.getSession();
        if (session != null) {
            node.put("rounds", session.getRounds());
            node.put("timeLimit", session.getTimeLimit());
            node.put("wordLength", session.getWordLength());
            node.put("currentRound", session.getCurrentRound());
            node.put("hostId", session.getHost().getId());
            node.put("remainingTime", computeRemainingTime(game, session.getTimeLimit()));
            node.set("players_status", buildPlayersStatus(session, game.getRoundNumber()));
            
            // Vérifier si le round est terminé
            boolean isRoundFinished = gameService.isRoundFinished(session, game.getRoundNumber());
            node.put("roundFinished", isRoundFinished);
            
            if (isRoundFinished) {
                // Déterminer la raison
                boolean timerExpired = gameService.isRoundTimerExpired(session, game.getRoundNumber());
                node.put("roundFinishedReason", timerExpired ? "TIMER" : "ALL_PLAYERS_FINISHED");
            }
        }
        
        // Ajouter les guesses (historique des tentatives)
        ArrayNode guessesArray = mapper.createArrayNode();
        if (game.getGuesses() != null) {
            for (com.wordle.backend.model.Guess guess : game.getGuesses()) {
                ObjectNode guessNode = mapper.createObjectNode();
                guessNode.put("attemptNo", guess.getAttemptNo());
                guessNode.put("guess", guess.getGuess());
                guessNode.put("resultMask", guess.getResultMask());
                guessesArray.add(guessNode);
            }
        }
        node.set("guesses", guessesArray);
        
        return node.toString();
    }

    private ArrayNode buildPlayersStatus(Session session, Integer roundNumber) {
        ArrayNode playersStatusArray = mapper.createArrayNode();

        List<SessionPlayer> sessionPlayers = sessionPlayerRepository.findBySessionId(session.getId());
        List<com.wordle.backend.model.Game> roundGames = new ArrayList<>();
        if (roundNumber != null) {
            roundGames = gameService.updateRoundGamesStatusIfTimedOut(session.getId(), roundNumber, session.getTimeLimit());
        }

        Map<Long, com.wordle.backend.model.Game.GameStatus> statusByUserId = new HashMap<>();
        for (com.wordle.backend.model.Game roundGame : roundGames) {
            if (roundGame.getUser() != null) {
                statusByUserId.put(roundGame.getUser().getId(), roundGame.getStatus());
            }
        }

        for (SessionPlayer sessionPlayer : sessionPlayers) {
            User user = sessionPlayer.getUser();
            ObjectNode playerNode = mapper.createObjectNode();
            playerNode.put("id", user.getId());
            playerNode.put("name", user.getName());
            playerNode.put("picture", user.getPicture());
            playerNode.put("isHost", sessionPlayer.isHost());

            com.wordle.backend.model.Game.GameStatus status = statusByUserId.get(user.getId());
            playerNode.put("status", status != null ? status.toString() : "IN_PROGRESS");
            playersStatusArray.add(playerNode);
        }

        return playersStatusArray;
    }

    public String roundFinished(String sessionCode, Integer roundNumber, String reason, boolean hasNextRound, String answer) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "round_finished");
        node.put("sessionCode", sessionCode);
        node.put("roundNumber", roundNumber != null ? roundNumber : 0);
        node.put("reason", reason != null ? reason : "UNKNOWN");
        node.put("hasNextRound", hasNextRound);
        node.put("answer", answer != null ? answer : "");
        return node.toString();
    }

    public String sessionFinished(String sessionCode) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "session_finished");
        node.put("sessionCode", sessionCode);
        return node.toString();
    }

    public String showLeaderboard(String sessionCode) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "show_leaderboard");
        node.put("sessionCode", sessionCode);
        return node.toString();
    }

    private int computeRemainingTime(com.wordle.backend.model.Game game, int timeLimit) {
        LocalDateTime startedAt = game.getCreatedAt();
        if (startedAt == null) {
            return Math.max(0, timeLimit);
        }

        long elapsedSeconds = Duration.between(startedAt, LocalDateTime.now()).getSeconds();
        long remaining = (long) timeLimit - elapsedSeconds;
        if (remaining < 0) {
            return 0;
        }
        if (remaining > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) remaining;
    }
    
    public String submitGuessResponse(com.wordle.backend.model.Game game, com.wordle.backend.model.Guess lastGuess) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "guess_submitted");
        node.put("gameId", game.getId().toString());
        node.put("word", lastGuess.getGuess());
        node.put("resultMask", lastGuess.getResultMask());
        node.put("attemptsUsed", game.getAttemptsUsed());
        node.put("maxAttempts", game.getMaxAttempts());
        node.put("status", game.getStatus().toString());
        
        return node.toString();
    }

    public String gameStatusUpdated(com.wordle.backend.model.Game game, User user) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "game_status_updated");
        node.put("gameId", game.getId().toString());
        node.put("userId", user.getId());
        node.put("userName", user.getName());
        node.put("status", game.getStatus().toString());
        node.put("roundNumber", game.getRoundNumber());
        node.put("attemptsUsed", game.getAttemptsUsed());
        
        return node.toString();
    }
}