package com.wordle.backend.application;

import com.wordle.backend.model.Session;
import com.wordle.backend.model.User;
import com.wordle.backend.model.SessionPlayer;
import com.wordle.backend.model.SessionPlayerId;
import com.wordle.backend.model.SessionChat;
import com.wordle.backend.service.SessionService;
import com.wordle.backend.service.SessionPlayerService;
import com.wordle.backend.service.SessionChatService;
import com.wordle.backend.repository.SessionRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class LobbyService {

    private final SessionService sessionService;
    private final SessionPlayerService sessionPlayerService;
    private final SessionChatService sessionChatService;
    private final SessionRepository sessionRepository;

    public LobbyService(SessionService sessionService,
                        SessionPlayerService sessionPlayerService,
                        SessionChatService sessionChatService,
                        SessionRepository sessionRepository) {
        this.sessionService = sessionService;
        this.sessionPlayerService = sessionPlayerService;
        this.sessionChatService = sessionChatService;
        this.sessionRepository = sessionRepository;
    }

    public Session createSession(User user, int rounds, int timeLimit, int wordLength) {

        var activeSessionOpt = sessionService.getActiveSessionForUser(user.getId());
        if (activeSessionOpt.isPresent()) {
            String code = activeSessionOpt.get().getCode();
            throw new IllegalStateException("User already has an active session;code=" + code);
        }

        Session session = new Session();
        session.setHost(user);
        session.setRounds(rounds);
        session.setTimeLimit(timeLimit);
        session.setWordLength(wordLength);
        session.setStatus("LOBBY");
        session.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        return sessionService.createSession(session);
    }
    public Session getSessionByCode(String code) {
        System.out.println("Recherche de session dans la fonction getSessionByCode pour code: " + code);
        Session session = null;
        try {
            
            session = sessionRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Session non trouvée pour le code: " + code));
            // Force le chargement du host (évite LazyInitializationException)
            if (session.getHost() != null) {
                String hostName = session.getHost().getName();
                System.out.println("Nom de l'hôte chargé: " + hostName);
            }
            System.out.println("Session trouvée dans LobbyService: " + session);
        } catch (Exception e) {
            System.out.println("Exception dans getSessionByCode: " + e);
            e.printStackTrace();
        }
        
        return session;
    }
    public Session joinSession(User user, String code) {

        Session session = sessionService.getSessionByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        boolean already = sessionPlayerService.getSessionsByUser(user.getId())
                .stream()
                .anyMatch(sp -> sp.getSession().getId().equals(session.getId()));

        SessionPlayer sp = null;
        if (!already) {
            sp = new SessionPlayer();
            sp.setId(new SessionPlayerId(session.getId(), user.getId()));
            sp.setSession(session);
            sp.setUser(user);
            sp.setHost(session.getHost().getId().equals(user.getId()));
            sessionPlayerService.addPlayer(sp);
        } else {
            // Récupère le joueur existant si besoin
            sp = sessionPlayerService.getSessionsByUser(user.getId())
                .stream()
                .filter(p -> p.getSession().getId().equals(session.getId()))
                .findFirst()
                .orElse(null);
            // Tu peux alors mettre à jour sp ici
            if (sp != null) {
                // Met à jour le statut d'hôte si besoin
                boolean isHost = session.getHost().getId().equals(user.getId());
                if (sp.isHost() != isHost) {
                    sp.setHost(isHost);
                    sessionPlayerService.addPlayer(sp); 
                }
            }

        }

        return session;
    }

    public void saveChat(Session session, User user, String message) {
        SessionChat chat = new SessionChat();
        chat.setSession(session);
        chat.setUser(user);
        chat.setMessage(message);
        chat.setSentAt(LocalDateTime.now());
        sessionChatService.saveMessage(chat);
    }
    public void leaveSession(User user, String sessionCode) {
        Session session = sessionService.getSessionByCode(sessionCode)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        SessionPlayerId spId = new SessionPlayerId(session.getId(), user.getId());
        sessionPlayerService.removePlayer(spId);
    }
}
