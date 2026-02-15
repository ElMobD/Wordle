package com.wordle.backend.repository;

import com.wordle.backend.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {

    List<Game> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Game> findByUserIdAndGameTypeAndStatus(Long userId, Game.GameType gameType, Game.GameStatus status);

    List<Game> findByUserIdAndGameTypeOrderByCreatedAtDesc(Long userId, Game.GameType gameType);
}
