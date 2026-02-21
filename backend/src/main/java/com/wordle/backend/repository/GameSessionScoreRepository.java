package com.wordle.backend.repository;

import com.wordle.backend.model.GameSessionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameSessionScoreRepository extends JpaRepository<GameSessionScore, Long> {

    @Query("SELECT s FROM GameSessionScore s JOIN FETCH s.user WHERE s.session.id = :sessionId")
    List<GameSessionScore> findBySessionId(@Param("sessionId") Long sessionId);

    Optional<GameSessionScore> findBySessionIdAndUserId(Long sessionId, Long userId);
}
