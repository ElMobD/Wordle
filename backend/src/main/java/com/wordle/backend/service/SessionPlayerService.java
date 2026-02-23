package com.wordle.backend.service;

import com.wordle.backend.model.SessionPlayer;
import com.wordle.backend.model.SessionPlayerId;
import com.wordle.backend.repository.SessionPlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SessionPlayerService {
    @Autowired
    private SessionPlayerRepository sessionPlayerRepository;

    public SessionPlayer addPlayer(SessionPlayer sessionPlayer) {
        return sessionPlayerRepository.save(sessionPlayer);
    }

    public void removePlayer(SessionPlayerId id) {
        sessionPlayerRepository.deleteById(id);
    }

    public List<SessionPlayer> getPlayersBySession(UUID sessionId) {
        return sessionPlayerRepository.findBySessionId(sessionId);
    }

    public List<SessionPlayer> getSessionsByUser(Long userId) {
        return sessionPlayerRepository.findByUserId(userId);
    }
}
