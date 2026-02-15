package com.wordle.backend.repository;

import com.wordle.backend.model.Guess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuessRepository extends JpaRepository<Guess, Long> {

    List<Guess> findByGameIdOrderByAttemptNo(UUID gameId);

    int countByGameId(UUID gameId);
}
