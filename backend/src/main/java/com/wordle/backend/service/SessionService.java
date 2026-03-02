package com.wordle.backend.service;

import com.wordle.backend.model.Session;
import com.wordle.backend.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wordle.backend.repository.SessionPlayerRepository;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {
    @Autowired
    private SessionPlayerRepository sessionPlayerRepository;

    public String getSessionCodeForUserId(Long userId) {
        if (userId == null) return null;
        java.util.List<com.wordle.backend.model.SessionPlayer> players = sessionPlayerRepository.findByUserId(userId);
        System.out.println("SessionPlayers trouvés pour userId=" + userId + ": " + players.size());
        for (com.wordle.backend.model.SessionPlayer sp : players) {
            java.util.UUID sessionId = sp.getId().getSessionId();
            com.wordle.backend.model.Session session = sessionRepository.findById(sessionId).orElse(null);
            System.out.println("SessionPlayer: session=" + (session != null ? session.getId() : null) + ", code=" + (session != null ? session.getCode() : null) + ", status=" + (session != null ? session.getStatus() : null));
            if (session != null && !"FINISHED".equals(session.getStatus()) && !"CANCELLED".equals(session.getStatus())) {
                System.out.println("Session active trouvée: code=" + session.getCode());
                return session.getCode();
            }
        }
        System.out.println("Aucune session active trouvée pour userId=" + userId);
        return null;
    }
    // Supprime l'ancienne version, ne garder que celle qui regarde session_players
    @Autowired
    private SessionRepository sessionRepository;

    public Session createSession(Session session) {
        session.setId(UUID.randomUUID());
        return sessionRepository.save(session);
    }

    public Optional<Session> getSessionByCode(String code) {
        return sessionRepository.findByCode(code);
    }

    public Optional<Session> getSessionById(UUID id) {
        return sessionRepository.findById(id);
    }

    public Session save(Session session) {
        return sessionRepository.save(session);
    }
    public boolean userHasActiveSession(Long hostId) {
        // On considère FINISHED et CANCELLED comme sessions terminées
        return sessionRepository.existsByHostIdAndStatusNotIn(hostId, java.util.Arrays.asList("FINISHED", "CANCELLED"));
    }

    public Optional<Session> getActiveSessionForUser(Long userId) {
        if (userId == null) return Optional.empty();
        java.util.List<com.wordle.backend.model.SessionPlayer> players = sessionPlayerRepository.findByUserId(userId);
        for (com.wordle.backend.model.SessionPlayer sp : players) {
            java.util.UUID sessionId = sp.getId().getSessionId();
            com.wordle.backend.model.Session session = sessionRepository.findById(sessionId).orElse(null);
            if (session != null && !"FINISHED".equals(session.getStatus()) && !"CANCELLED".equals(session.getStatus())) {
                return Optional.of(session);
            }
        }
        return Optional.empty();
    }

    public String getSessionCodeForUser(com.wordle.backend.model.User user) {
        if (user == null || user.getId() == null) return null;
        Optional<Session> sessionOpt = getActiveSessionForUser(user.getId());
        return sessionOpt.map(Session::getCode).orElse(null);
    }

    public String deleteSession(String code) {
        Optional<Session> sessionOpt = sessionRepository.findByCode(code);
        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            // Vérifier qu'il n'y a plus de joueurs dans la session
            java.util.UUID sessionId = session.getId();
            java.util.List<com.wordle.backend.model.SessionPlayer> players = sessionPlayerRepository.findBySessionId(sessionId);
            if (players != null && !players.isEmpty()) {
                return "Impossible d'annuler : des joueurs sont encore dans la session.";
            }
            session.setStatus("CANCELLED");
            sessionRepository.save(session);
            return "Session annulée avec succès";
        } else {
            return "Session non trouvée pour le code: " + code;
        }
    }
}
