package com.wordle.backend.repository;

import com.wordle.backend.model.SessionGamePlayer;
import com.wordle.backend.model.SessionGamePlayerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionGamePlayerRepository extends JpaRepository<SessionGamePlayer, SessionGamePlayerId> {
    
    /**
     * Récupère tous les enregistrements pour une game donnée
     */
    List<SessionGamePlayer> findByIdGameId(UUID gameId);
    
    /**
     * Récupère tous les enregistrements pour une session donnée
     */
    List<SessionGamePlayer> findByIdSessionId(UUID sessionId);
    
    /**
     * Récupère l'enregistrement d'un joueur pour une game donnée
     */
    SessionGamePlayer findByIdSessionIdAndIdGameIdAndIdUserId(UUID sessionId, UUID gameId, Long userId);
}
