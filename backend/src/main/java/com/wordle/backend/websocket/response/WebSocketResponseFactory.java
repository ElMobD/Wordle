package com.wordle.backend.websocket.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordle.backend.model.Session;
import com.wordle.backend.model.SessionPlayer;
import com.wordle.backend.model.User;
import com.wordle.backend.repository.SessionPlayerRepository;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class WebSocketResponseFactory {
    @Autowired
    private SessionPlayerRepository sessionPlayerRepository;
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

    public String playerJoined(User user) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "player_joined");
        node.put("userId", user.getId());
        node.put("userName", user.getName());
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
}