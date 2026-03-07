package com.wordle.backend.repository;

import com.wordle.backend.model.SessionScore;
import com.wordle.backend.model.SessionScoreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionScoreRepository extends JpaRepository<SessionScore, SessionScoreId> {
    
    // Récupérer tous les scores d'une session, triés par total_score DESC
    @Query("SELECT s FROM SessionScore s WHERE s.id.sessionId = :sessionId ORDER BY s.totalScore DESC, s.totalTimeRemaining DESC")
    List<SessionScore> findBySessionIdOrderByTotalScoreDesc(@Param("sessionId") UUID sessionId);
    
    // Récupérer le score d'un joueur spécifique dans une session
    @Query("SELECT s FROM SessionScore s WHERE s.id.sessionId = :sessionId AND s.id.userId = :userId")
    Optional<SessionScore> findBySessionIdAndUserId(@Param("sessionId") UUID sessionId, @Param("userId") Long userId);
    
    // Récupérer tous les scores d'un joueur (historique)
    @Query("SELECT s FROM SessionScore s WHERE s.id.userId = :userId ORDER BY s.updatedAt DESC")
    List<SessionScore> findByUserId(@Param("userId") Long userId);
    
    // Récupérer le classement avec rang
    @Query(value = """
        SELECT s.*, 
               RANK() OVER (ORDER BY s.total_score DESC, s.total_time_remaining DESC) as rank
        FROM session_scores s
        WHERE s.session_id = :sessionId
        ORDER BY rank
        """, nativeQuery = true)
    List<Object[]> findRankingBySessionId(@Param("sessionId") UUID sessionId);
}
