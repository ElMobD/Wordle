package com.wordle.backend.repository;

import com.wordle.backend.model.SessionPlayer;
import com.wordle.backend.model.SessionPlayerId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionPlayerRepository extends JpaRepository<SessionPlayer, SessionPlayerId> {
    List<SessionPlayer> findBySessionId(java.util.UUID sessionId);
    List<SessionPlayer> findByUserId(Long userId);
}
