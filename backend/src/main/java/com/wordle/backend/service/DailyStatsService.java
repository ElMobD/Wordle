package com.wordle.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordle.backend.model.DailyStats;
import com.wordle.backend.model.Game;
import com.wordle.backend.repository.DailyStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class DailyStatsService {

    private static final Logger logger = Logger.getLogger(DailyStatsService.class.getName());

    private final DailyStatsRepository dailyStatsRepository;
    private final ObjectMapper objectMapper;

    public DailyStatsService(DailyStatsRepository dailyStatsRepository, ObjectMapper objectMapper) {
        this.dailyStatsRepository = dailyStatsRepository;
        this.objectMapper = objectMapper;
    }

    public DailyStats getOrCreateStats(Long userId) {
        Optional<DailyStats> existing = dailyStatsRepository.findById(userId);
        if (existing.isPresent()) {
            DailyStats stats = existing.get();
            normalizeDistributionIfNeeded(stats);
            return stats;
        }

        DailyStats stats = new DailyStats(userId);
        stats.setGuessDistribution(defaultDistributionJson());
        stats.setUpdatedAt(LocalDateTime.now());
        return dailyStatsRepository.save(stats);
    }

    public DailyStats getUserStats(Long userId) {
        return getOrCreateStats(userId);
    }

    @Transactional
    public DailyStats recordDailyGameResult(Long userId, Game game) {
        if (game == null || game.getGameType() != Game.GameType.DAILY) {
            throw new IllegalArgumentException("Le jeu doit etre de type DAILY");
        }

        if (game.getStatus() != Game.GameStatus.WON && game.getStatus() != Game.GameStatus.LOST) {
            throw new IllegalArgumentException("Le jeu DAILY doit etre termine (WON ou LOST)");
        }

        DailyStats stats = getOrCreateStats(userId);
        LocalDate playedDate = game.getCreatedAt() != null ? game.getCreatedAt().toLocalDate() : LocalDate.now();

        // Evite un double comptage de la meme partie quotidienne.
        if (playedDate.equals(stats.getLastPlayedDate())) {
            return stats;
        }

        stats.setTotalPlayed(safeInt(stats.getTotalPlayed()) + 1);
        stats.setLastPlayedDate(playedDate);

        boolean won = game.getStatus() == Game.GameStatus.WON;
        if (won) {
            stats.setTotalWon(safeInt(stats.getTotalWon()) + 1);
            updateWinStreak(stats, playedDate);
            incrementDistribution(stats, game.getAttemptsUsed());
            stats.setLastWinDate(playedDate);
        } else {
            stats.setCurrentStreak(0);
        }

        stats.setUpdatedAt(LocalDateTime.now());

        DailyStats saved = dailyStatsRepository.save(stats);
        logger.info("Daily stats updated for userId=" + userId
                + " totalPlayed=" + saved.getTotalPlayed()
                + " totalWon=" + saved.getTotalWon());
        return saved;
    }

    private void updateWinStreak(DailyStats stats, LocalDate playedDate) {
        LocalDate lastWinDate = stats.getLastWinDate();

        if (lastWinDate != null && lastWinDate.plusDays(1).equals(playedDate)) {
            stats.setCurrentStreak(safeInt(stats.getCurrentStreak()) + 1);
        } else {
            stats.setCurrentStreak(1);
        }

        if (safeInt(stats.getCurrentStreak()) > safeInt(stats.getMaxStreak())) {
            stats.setMaxStreak(stats.getCurrentStreak());
        }
    }

    private void incrementDistribution(DailyStats stats, Integer attemptsUsed) {
        int attempt = attemptsUsed == null ? 0 : attemptsUsed;
        if (attempt < 1 || attempt > 6) {
            return;
        }

        Map<String, Integer> distribution = parseDistribution(stats.getGuessDistribution());
        String key = String.valueOf(attempt);
        distribution.put(key, safeInt(distribution.get(key)) + 1);
        stats.setGuessDistribution(toDistributionJson(distribution));
    }

    private void normalizeDistributionIfNeeded(DailyStats stats) {
        Map<String, Integer> distribution = parseDistribution(stats.getGuessDistribution());
        boolean changed = false;

        for (int i = 1; i <= 6; i++) {
            String key = String.valueOf(i);
            if (!distribution.containsKey(key)) {
                distribution.put(key, 0);
                changed = true;
            }
        }

        if (changed) {
            stats.setGuessDistribution(toDistributionJson(distribution));
            stats.setUpdatedAt(LocalDateTime.now());
            dailyStatsRepository.save(stats);
        }
    }

    private Map<String, Integer> parseDistribution(String json) {
        try {
            Map<String, Integer> parsed = objectMapper.readValue(
                    json == null ? defaultDistributionJson() : json,
                    new TypeReference<Map<String, Integer>>() {
                    }
            );

            Map<String, Integer> ordered = new LinkedHashMap<>();
            for (int i = 1; i <= 6; i++) {
                String key = String.valueOf(i);
                ordered.put(key, safeInt(parsed.get(key)));
            }
            return ordered;
        } catch (Exception e) {
            return defaultDistributionMap();
        }
    }

    private String toDistributionJson(Map<String, Integer> distribution) {
        try {
            return objectMapper.writeValueAsString(distribution);
        } catch (Exception e) {
            return defaultDistributionJson();
        }
    }

    private Map<String, Integer> defaultDistributionMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 1; i <= 6; i++) {
            map.put(String.valueOf(i), 0);
        }
        return map;
    }

    private String defaultDistributionJson() {
        return "{\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":0}";
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
