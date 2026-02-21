package com.wordle.backend.repository;

import com.wordle.backend.model.GameSessionRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameSessionRoundRepository extends JpaRepository<GameSessionRound, Long> {

    List<GameSessionRound> findBySessionIdOrderByRoundIndexAsc(Long sessionId);

    Optional<GameSessionRound> findBySessionIdAndRoundIndex(Long sessionId, Integer roundIndex);
}
