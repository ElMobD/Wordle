package com.wordle.backend.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import com.wordle.backend.application.LobbyService;
import com.wordle.backend.websocket.response.WebSocketResponseFactory;

import net.minidev.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import com.wordle.backend.service.*;
import com.wordle.backend.model.User;
import com.wordle.backend.model.Session;
import com.wordle.backend.websocket.dto.MessageType;
import com.wordle.backend.websocket.dto.CreateSessionRequest;
import com.wordle.backend.websocket.dto.JoinSessionRequest;
import com.wordle.backend.websocket.dto.ChatMessageRequest;

@Component
public class LobbyWebSocketHandler extends TextWebSocketHandler {

    private final LobbyService lobbyService;
    private final SessionService sessionService;
    private final JwtValidator jwtValidator;
    private final UserService userService;
    private final WebSocketResponseFactory responseFactory;
    private final ObjectMapper mapper;
    private static final Logger logger = Logger.getLogger(LobbyWebSocketHandler.class.getName());

    private final Map<String, Set<WebSocketSession>> lobbies = new ConcurrentHashMap<>();

    public LobbyWebSocketHandler(LobbyService lobbyService,
                                 JwtValidator jwtValidator,
                                 UserService userService,
                                 SessionService sessionService,
                                 WebSocketResponseFactory responseFactory,
                                 ObjectMapper mapper) {
        this.lobbyService = lobbyService;
        this.sessionService = sessionService;
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
            JSONObject response = new JSONObject();
            response.put("userId", user.getId());
            session.sendMessage(new TextMessage(response.toString()));
        } catch (Exception e) {
            session.close();
        }
        System.out.println("Connexion WebSocket établie avec session ID: " + session.getId());
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
                        broadcast(s.getCode(), responseFactory.playerJoined(user, s.getCode()));
                    } catch (IllegalArgumentException e) {
                        session.sendMessage(new TextMessage(responseFactory.error(e.getMessage())));
                    }
                }
                case CHAT -> {                    
                    ChatMessageRequest req = mapper.treeToValue(root, ChatMessageRequest.class);
                    logger.info("Received chat message from user " + user.getName() + " in session " + req.getSessionCode() + ": " + req.getMessage());
                    try {
                        logger.info("Saving chat message for session " + req.getSessionCode() + " and user " + user.getName());
                        lobbyService.saveChat(
                            lobbyService.joinSession(user, req.getSessionCode()),
                            user,
                            req.getMessage()
                        );
                        logger.info("Broadcasting chat message to session " + req.getSessionCode() + ": " + req.getMessage());
                        broadcast(req.getSessionCode(), responseFactory.chat(user, req.getMessage()));
                        logger.info("Chat message broadcasted successfully for session " + req.getSessionCode());
                    } catch (Exception e) {
                        logger.severe("Error handling chat message for session " + req.getSessionCode() + ": " + e.getMessage());
                        session.sendMessage(new TextMessage(responseFactory.error("Chat error: " + e.getMessage())));
                    }    
                }
                case LOBBYINFOS ->{
                    String sessionCode = root.has("sessionCode") ? root.get("sessionCode").asText() : null;
                    logger.info("Received LOBBYINFOS request for session code: " + sessionCode);
                    if (sessionCode == null) {
                        session.sendMessage(new TextMessage(responseFactory.error("Session code manquant pour LOBBYINFO")));
                        return;
                    }
                    try {
                        Session s = lobbyService.getSessionByCode(sessionCode);
                        logger.info("Session retrieved for LOBBYINFOS: " + s);
                        // Récupère les infos du lobby (joueurs, paramètres, etc.)
                        String lobbyInfo = responseFactory.lobbyInfo(s);
                        session.sendMessage(new TextMessage(lobbyInfo));
                    } catch (Exception e) {
                        logger.severe("Error retrieving lobby info for session code " + sessionCode + ": " + e.getMessage());
                        e.printStackTrace();
                        session.sendMessage(new TextMessage(responseFactory.error("Erreur lors de la récupération du lobby: " + e.getMessage())));
                    }
                }
                case LEAVE_LOBBY -> {
                    String sessionCode = root.has("sessionCode") ? root.get("sessionCode").asText() : null;
                    if (sessionCode == null) {
                        session.sendMessage(new TextMessage(responseFactory.error("Session code manquant pour LEAVE_LOBBY")));
                        return;
                    }
                    try {
                        lobbyService.leaveSession(user, sessionCode);
                        broadcast(sessionCode, responseFactory.playerLeft(user, sessionCode));
                        // Optionnel : retirer la session du lobby
                        lobbies.getOrDefault(sessionCode, Set.of()).remove(session);
                        // Tenter d'annuler la session si elle est vide
                        logger.info("Tentative d'annulation de la session après départ du joueur " + user.getName() + " pour session code=" + sessionCode);
                        String deleteMsg = sessionService.deleteSession(sessionCode);
                        System.out.println("Tentative d'annulation de la session après départ: " + deleteMsg);
                    } catch (Exception e) {
                        session.sendMessage(new TextMessage(responseFactory.error("Erreur lors de la sortie du lobby: " + e.getMessage())));
                    }
                }
                case START_GAME -> {
                    // À implémenter : vérifier que l'utilisateur est l'hôte, changer le statut de la session, etc.
                    session.sendMessage(new TextMessage(responseFactory.error("START_GAME non implémenté")));
                }
                case PING -> {
                    // Juste pour tester la connexion, pas besoin de faire quoi que ce soit
                    logger.info("Received ping from user " + user.getName());
                    session.sendMessage(new TextMessage(responseFactory.pong()));
                }
                case GET_CHAT_HISTORY -> {
                    String sessionCode = root.has("sessionCode") ? root.get("sessionCode").asText() : null;
                    if (sessionCode == null) {
                        if (session.isOpen()) {
                            try {
                                session.sendMessage(new TextMessage(responseFactory.error("Session code manquant pour GET_CHAT_HISTORY")));
                            } catch (Exception e) {
                                logger.severe("Erreur lors de l'envoi du message WebSocket: " + e.getMessage());
                            }
                        }
                        return;
                    }
                    try {
                        Session s = lobbyService.getSessionByCode(sessionCode);
                        String chatHistory = responseFactory.chatHistory(s);
                        if (session.isOpen()) {
                            try {
                                session.sendMessage(new TextMessage(chatHistory));
                            } catch (Exception e) {
                                logger.severe("Erreur lors de l'envoi du message WebSocket: " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        if (session.isOpen()) {
                            try {
                                session.sendMessage(new TextMessage(responseFactory.error("Erreur lors de la récupération de l'historique du chat: " + e.getMessage())));
                            } catch (Exception ex) {
                                logger.severe("Erreur lors de l'envoi du message WebSocket: " + ex.getMessage());
                            }
                        }
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
    //logger.info("[WebSocket] Headers: " + session.getHandshakeHeaders());
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        //System.out.println("[WebSocket] Headers: " + session.getHandshakeHeaders());
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
