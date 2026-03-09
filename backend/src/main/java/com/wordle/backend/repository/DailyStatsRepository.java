package com.wordle.backend.repository;

import com.wordle.backend.model.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {

    Optional<DailyStats> findByUserId(Long userId);
}
