package com.wordle.backend.websocket;

import com.wordle.backend.service.JwtValidator;
import com.wordle.backend.service.UserService;
import com.wordle.backend.model.User;
import io.jsonwebtoken.Claims;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordle.backend.model.Session;
import com.wordle.backend.model.SessionPlayer;
import com.wordle.backend.model.SessionPlayerId;
import com.wordle.backend.service.SessionPlayerService;
import com.wordle.backend.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LobbyWebSocketHandler extends TextWebSocketHandler {
    // Map sessionCode -> Set of WebSocketSession
    private final Map<String, Set<WebSocketSession>> lobbySessions = new ConcurrentHashMap<>();

    @Autowired
    private SessionService sessionService;
    @Autowired
    private SessionPlayerService sessionPlayerService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    private JwtValidator jwtValidator;

    @Autowired
    private UserService userService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Authentification via header Authorization: Bearer <token>
        System.out.println("[WebSocket] Headers: " + session.getHandshakeHeaders());
        String authHeader = session.getHandshakeHeaders().getFirst("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("[WebSocket] Authorization header reçu: " + authHeader);
        } else {
            // Essaye de récupérer le token depuis le cookie
            String cookieHeader = session.getHandshakeHeaders().getFirst("cookie");
            System.out.println("[WebSocket] Cookie header reçu: " + cookieHeader);
            if (cookieHeader != null) {
                for (String cookie : cookieHeader.split(";")) {
                    String[] parts = cookie.trim().split("=", 2);
                    if (parts.length == 2 && parts[0].equals("token")) {
                        token = parts[1];
                        System.out.println("[WebSocket] Token extrait du cookie: " + token);
                        break;
                    }
                }
            }
        }
        if (token == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing or invalid Authorization token (header/cookie)"));
            return;
        }
        try {
            Claims claims = jwtValidator.validateToken(token);
            String email = claims.getSubject();
            User user = userService.findByEmail(email).orElse(null);
            if (user == null) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("User not found"));
                return;
            }
            // Tu peux stocker l'utilisateur dans les attributs de la session si besoin
            session.getAttributes().put("user", user);
        } catch (Exception e) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token: " + e.getMessage()));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("[WebSocket] Message reçu: " + message.getPayload());
        User user = (User) session.getAttributes().get("user");
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            String type = node.has("type") ? node.get("type").asText() : null;

            if ("create".equals(type)) {
                // Vérifier si l'utilisateur est déjà host d'une session non terminée
                // Si l'utilisateur a déjà une session active, renvoyer le code de cette session
                java.util.Optional<com.wordle.backend.model.Session> activeSessionOpt = sessionService.getActiveSessionForUser(user.getId());
                if (activeSessionOpt.isPresent()) {
                    ObjectNode error = objectMapper.createObjectNode();
                    error.put("type", "error");
                    error.put("message", "You already have an active session. Finish or cancel it before creating a new one.");
                    error.put("sessionCode", activeSessionOpt.get().getCode());
                    session.sendMessage(new TextMessage(error.toString()));
                    return;
                }
                // Création de session
                int rounds = node.has("rounds") ? node.get("rounds").asInt(5) : 5;
                int timeLimit = node.has("timeLimit") ? node.get("timeLimit").asInt(60) : 60;
                int wordLength = node.has("wordLength") ? node.get("wordLength").asInt(5) : 5;
                Session sessionEntity = new Session();
                sessionEntity.setHost(user);
                sessionEntity.setRounds(rounds);
                sessionEntity.setTimeLimit(timeLimit);
                sessionEntity.setWordLength(wordLength);
                sessionEntity.setStatus("LOBBY");
                sessionEntity.setCode(generateSessionCode());
                sessionEntity = sessionService.createSession(sessionEntity);

                // Ajout de l'hôte dans la map et la table session_players
                lobbySessions.computeIfAbsent(sessionEntity.getCode(), k -> new HashSet<>()).add(session);
                SessionPlayer sp = new SessionPlayer();
                sp.setId(new SessionPlayerId(sessionEntity.getId(), user.getId()));
                sp.setSession(sessionEntity);
                sp.setUser(user);
                sp.setHost(true);
                sessionPlayerService.addPlayer(sp);

                // Réponse au client
                ObjectNode response = objectMapper.createObjectNode();
                response.put("type", "session_created");
                response.put("sessionCode", sessionEntity.getCode());
                response.put("rounds", rounds);
                response.put("timeLimit", timeLimit);
                response.put("wordLength", wordLength);
                session.sendMessage(new TextMessage(response.toString()));
            } else if ("join".equals(type)) {
                // Rejoindre une session
                String sessionCode = node.get("sessionCode").asText();
                Session sessionEntity = requireSessionOrError(sessionCode, session);
                if (sessionEntity == null) return;
                // Empêcher l'hôte ou un joueur déjà présent de rejoindre à nouveau la session
                boolean isHost = sessionEntity.getHost().getId().equals(user.getId());
                boolean isAlreadyPlayer = sessionPlayerService.getSessionsByUser(user.getId()).stream()
                        .anyMatch(sp -> sp.getSession().getId().equals(sessionEntity.getId()));
                if (isHost || isAlreadyPlayer) {
                    ObjectNode error = objectMapper.createObjectNode();
                    error.put("type", "error");
                    error.put("message", "You are already in this session.");
                    session.sendMessage(new TextMessage(error.toString()));
                    return;
                }
                lobbySessions.computeIfAbsent(sessionCode, k -> new HashSet<>()).add(session);
                SessionPlayer sp = new SessionPlayer();
                sp.setId(new SessionPlayerId(sessionEntity.getId(), user.getId()));
                sp.setSession(sessionEntity);
                sp.setUser(user);
                sp.setHost(false);
                sessionPlayerService.addPlayer(sp);

                // Notifier tous les joueurs du lobby (exemple simplifié)
                ObjectNode joined = objectMapper.createObjectNode();
                joined.put("type", "player_joined");
                joined.put("userId", user.getId());
                joined.put("userName", user.getName());
                broadcastToSession(sessionCode, joined.toString());
            } else if ("chat".equals(type)) {
                // Gestion d'un message de chat dans le lobby
                String sessionCode = node.get("sessionCode").asText();
                String chatMessage = node.get("message").asText();
                Session sessionEntity = requireSessionOrError(sessionCode, session);
                if (sessionEntity == null) return;
                // Optionnel : sauvegarder le message en base (SessionChatService)
                // sessionChatService.saveMessage(sessionCode, user, chatMessage);

                ObjectNode chat = objectMapper.createObjectNode();
                chat.put("type", "chat");
                chat.put("userId", user.getId());
                chat.put("userName", user.getName());
                chat.put("message", chatMessage);
                broadcastToSession(sessionCode, chat.toString());
            } else {
                ObjectNode error = objectMapper.createObjectNode();
                error.put("type", "error");
                error.put("message", "Unknown message type: " + type);
                session.sendMessage(new TextMessage(error.toString()));
            }
        } catch (Exception e) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("type", "error");
            error.put("message", "Malformed message or internal error: " + e.getMessage());
            session.sendMessage(new TextMessage(error.toString()));
        }
    }

    // Génère un code de session aléatoire (exemple simple)
    private String generateSessionCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Logique pour retirer le joueur du lobby
    }

    // Méthode utilitaire pour envoyer un message à tous les joueurs d'une session
    private void broadcastToSession(String sessionCode, String payload) {
        Set<WebSocketSession> sessions = lobbySessions.get(sessionCode);
        if (sessions != null) {
            for (WebSocketSession s : sessions) {
                try {
                    s.sendMessage(new TextMessage(payload));
                } catch (Exception e) {
                    // Gestion d'erreur
                }
            }
        }
    }
    // Vérifie l'existence d'une session, renvoie null et envoie une erreur si non trouvée
    private Session requireSessionOrError(String sessionCode, WebSocketSession wsSession) throws Exception {
        Session sessionEntity = sessionService.getSessionByCode(sessionCode).orElse(null);
        if (sessionEntity == null) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("type", "error");
            error.put("message", "Session not found");
            wsSession.sendMessage(new TextMessage(error.toString()));
            return null;
        }
        return sessionEntity;
    }
}
