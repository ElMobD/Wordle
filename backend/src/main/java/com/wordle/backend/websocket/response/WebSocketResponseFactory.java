package com.wordle.backend.websocket.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordle.backend.model.Session;
import com.wordle.backend.model.SessionPlayer;
import com.wordle.backend.model.User;
import com.wordle.backend.repository.SessionPlayerRepository;
import com.wordle.backend.service.SessionChatService;
import com.wordle.backend.model.SessionChat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebSocketResponseFactory {
    @Autowired
    private SessionPlayerRepository sessionPlayerRepository;
    @Autowired
    private SessionChatService sessionChatService;
    private final ObjectMapper mapper;

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
        System.out.println("Génération de lobbyInfo pour session code=" + session.getCode());
        Map<String, Object> info = new HashMap<>();
        info.put("type", "lobbyInfo");
        info.put("sessionCode", session.getCode());
        info.put("rounds", session.getRounds());
        info.put("timeLimit", session.getTimeLimit());
        info.put("wordLength", session.getWordLength());
        info.put("status", session.getStatus());
        info.put("host", session.getHost().getName());
        info.put("hostId", session.getHost().getId());

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
}