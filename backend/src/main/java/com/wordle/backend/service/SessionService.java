package com.wordle.backend.service;

import com.wordle.backend.model.Session;
import com.wordle.backend.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {
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
        public Optional<Session> getActiveSessionForUser(Long hostId) {
        return sessionRepository.findFirstByHostIdAndStatusNotIn(hostId, java.util.Arrays.asList("FINISHED", "CANCELLED"));
    }
}
