package com.wordle.backend.repository;

import com.wordle.backend.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {

    // Parties solo d'un utilisateur
    List<Game> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Game> findByUserIdAndGameTypeAndStatus(Long userId, Game.GameType gameType, Game.GameStatus status);

    List<Game> findByUserIdAndGameTypeOrderByCreatedAtDesc(Long userId, Game.GameType gameType);

    // Trouver le daily de l'utilisateur créé aujourd'hui (peu importe son statut)
    @Query("SELECT g FROM Game g WHERE g.user.id = ?1 AND g.gameType = ?2 AND g.createdAt >= ?3 AND g.createdAt <= ?4")
    Optional<Game> findByUserIdAndGameTypeAndCreatedAtBetween(Long userId, Game.GameType gameType, LocalDateTime createdAfter, LocalDateTime createdBefore);

    // Parties multi d'une session (par round)
    Optional<Game> findBySessionIdAndRoundNumber(UUID sessionId, Integer roundNumber);

    // Toutes les games d'une session
    List<Game> findBySessionIdOrderByRoundNumber(UUID sessionId);
    
    // Game d'un joueur spécifique dans une session/round
    Optional<Game> findBySessionIdAndRoundNumberAndUserId(UUID sessionId, Integer roundNumber, Long userId);
    
    // Game d'un joueur avec Session chargée (pour éviter LazyInitializationException)
    @Query("SELECT g FROM Game g JOIN FETCH g.session WHERE g.session.id = ?1 AND g.roundNumber = ?2 AND g.user.id = ?3")
    Optional<Game> findBySessionIdAndRoundNumberAndUserIdWithSession(UUID sessionId, Integer roundNumber, Long userId);
    
    // Toutes les games d'un joueur dans une session
    List<Game> findBySessionIdAndUserId(UUID sessionId, Long userId);
}
