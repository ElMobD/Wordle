
package com.wordle.backend.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import com.wordle.backend.application.LobbyService;
import com.wordle.backend.service.JwtValidator;
import com.wordle.backend.service.UserService;
import com.wordle.backend.websocket.response.WebSocketResponseFactory;

import io.jsonwebtoken.Claims;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.wordle.backend.model.User;
import com.wordle.backend.model.Session;
import com.wordle.backend.websocket.dto.MessageType;
import com.wordle.backend.websocket.dto.CreateSessionRequest;
import com.wordle.backend.websocket.dto.JoinSessionRequest;
import com.wordle.backend.websocket.dto.ChatMessageRequest;

@Component
public class LobbyWebSocketHandler extends TextWebSocketHandler {

    private final LobbyService lobbyService;
    private final JwtValidator jwtValidator;
    private final UserService userService;
    private final WebSocketResponseFactory responseFactory;
    private final ObjectMapper mapper;

    private final Map<String, Set<WebSocketSession>> lobbies = new ConcurrentHashMap<>();

    public LobbyWebSocketHandler(LobbyService lobbyService,
                                 JwtValidator jwtValidator,
                                 UserService userService,
                                 WebSocketResponseFactory responseFactory,
                                 ObjectMapper mapper) {
        this.lobbyService = lobbyService;
        this.jwtValidator = jwtValidator;
        this.userService = userService;
        this.responseFactory = responseFactory;
        this.mapper = mapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            System.out.println("Tentative de connexion WebSocket avec session ID: " + session.getId());
            
            User user = validateWebSocketUser(session);
            session.getAttributes().put("user", user);
        } catch (Exception e) {
            System.out.println("Échec de validation du token pour la session ID: " + session.getId() + " - " + e.getMessage());
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User user = (User) session.getAttributes().get("user");
        try {
            JsonNode root = mapper.readTree(message.getPayload());
            MessageType type = MessageType.valueOf(root.get("type").asText().toUpperCase());
            switch (type) {
                case CREATE -> {
                    CreateSessionRequest req = mapper.treeToValue(root, CreateSessionRequest.class);
                    try {
                        Session s = lobbyService.createSession(user, req.getRounds(), req.getTimeLimit(), req.getWordLength());
                        addToLobby(s.getCode(), session);
                        session.sendMessage(new TextMessage(responseFactory.sessionCreated(s)));
                    } catch (IllegalStateException e) {
                        String msg = e.getMessage();
                        String code = null;
                        if (msg != null && msg.contains(";code=")) {
                            String[] parts = msg.split(";code=");
                            msg = parts[0];
                            code = parts.length > 1 ? parts[1] : null;
                        }
                        session.sendMessage(new TextMessage(responseFactory.errorWithSessionCode(msg, code)));
                    }
                }
                case JOIN -> {
                    JoinSessionRequest req = mapper.treeToValue(root, JoinSessionRequest.class);
                    try {
                        Session s = lobbyService.joinSession(user, req.getSessionCode());
                        addToLobby(s.getCode(), session);
                        broadcast(s.getCode(), responseFactory.playerJoined(user));
                    } catch (IllegalArgumentException e) {
                        session.sendMessage(new TextMessage(responseFactory.error(e.getMessage())));
                    }
                }
                case CHAT -> {
                    ChatMessageRequest req = mapper.treeToValue(root, ChatMessageRequest.class);
                    try {
                        lobbyService.saveChat(
                            lobbyService.joinSession(user, req.getSessionCode()),
                            user,
                            req.getMessage()
                        );
                        System.out.println("Broadcasting chat message from user " + user.getName() + " in session " + req.getSessionCode() + ": " + req.getMessage());
                        broadcast(req.getSessionCode(), responseFactory.chat(user, req.getMessage()));
                    } catch (Exception e) {
                        System.out.println("Il y a une erreur dans le broadcasting");
                        session.sendMessage(new TextMessage(responseFactory.error("Chat error: " + e.getMessage())));
                    }
                }
            }
        } catch (Exception e) {
            session.sendMessage(new TextMessage(responseFactory.error("Malformed message or internal error: " + e.getMessage())));
        }
    }

    private void addToLobby(String code, WebSocketSession session) {
        lobbies.computeIfAbsent(code, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    private void broadcast(String code, String payload) throws Exception {
        System.out.println("Broadcasting message to lobby with code: " + code + " - Payload: " + payload);
        System.out.println("Taille du lobby avant broadcast: " + lobbies.getOrDefault(code, Set.of()).size());
        for (WebSocketSession ws : lobbies.getOrDefault(code, Set.of())) {
            System.out.println("Broadcasting to session ID: " + ws.getId() + " - Payload: " + payload);
            ws.sendMessage(new TextMessage(payload));
        }
        System.out.println("Fin du Broadcast pour le code: " + code);
    }

    private User validateWebSocketUser(WebSocketSession session) throws Exception {
    String token = null;
    String authHeader = session.getHandshakeHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        System.out.println("[WebSocket] Headers: " + session.getHandshakeHeaders());
        token = authHeader.substring(7);
    } else {
        String cookieHeader = session.getHandshakeHeaders().getFirst("cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && parts[0].equals("token")) {
                    token = parts[1];
                    break;
                }
            }
        }
    }
    if (token == null) throw new Exception("Missing token");
    io.jsonwebtoken.Claims claims = jwtValidator.validateToken(token);
    String email = claims.getSubject();
    User user = userService.findByEmail(email).orElse(null);
    if (user == null) throw new Exception("User not found");
    return user;
}

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Optionnel : retirer la session de toutes les lobbies
        lobbies.values().forEach(sessions -> sessions.remove(session));
    }
}
