package com.wordle.backend.websocket.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordle.backend.model.Session;
import com.wordle.backend.model.User;
import org.springframework.stereotype.Component;

@Component
public class WebSocketResponseFactory {

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
}