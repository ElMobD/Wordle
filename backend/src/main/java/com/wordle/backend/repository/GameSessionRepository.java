package com.wordle.backend.repository;

import com.wordle.backend.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    @Query("SELECT s FROM GameSession s JOIN FETCH s.host WHERE s.code = :code")
    Optional<GameSession> findByCode(@Param("code") String code);
}
