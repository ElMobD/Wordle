package com.wordle.backend.repository;

import com.wordle.backend.model.SessionPlayer;
import com.wordle.backend.model.SessionPlayerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface SessionPlayerRepository extends JpaRepository<SessionPlayer, SessionPlayerId> {
    @EntityGraph(attributePaths = {"user"})
    List<SessionPlayer> findBySessionId(java.util.UUID sessionId);
    List<SessionPlayer> findByUserId(Long userId);
}
