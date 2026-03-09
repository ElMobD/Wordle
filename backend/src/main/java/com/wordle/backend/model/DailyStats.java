package com.wordle.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_stats")
public class DailyStats {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_played", nullable = false)
    private Integer totalPlayed = 0;

    @Column(name = "total_won", nullable = false)
    private Integer totalWon = 0;

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0;

    @Column(name = "max_streak", nullable = false)
    private Integer maxStreak = 0;

    @Column(name = "guess_distribution", nullable = false, columnDefinition = "jsonb")
    private String guessDistribution = "{\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":0}";

    @Column(name = "last_played_date")
    private LocalDate lastPlayedDate;

    @Column(name = "last_win_date")
    private LocalDate lastWinDate;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public DailyStats() {
    }

    public DailyStats(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getTotalPlayed() {
        return totalPlayed;
    }

    public void setTotalPlayed(Integer totalPlayed) {
        this.totalPlayed = totalPlayed;
    }

    public Integer getTotalWon() {
        return totalWon;
    }

    public void setTotalWon(Integer totalWon) {
        this.totalWon = totalWon;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getMaxStreak() {
        return maxStreak;
    }

    public void setMaxStreak(Integer maxStreak) {
        this.maxStreak = maxStreak;
    }

    public String getGuessDistribution() {
        return guessDistribution;
    }

    public void setGuessDistribution(String guessDistribution) {
        this.guessDistribution = guessDistribution;
    }

    public LocalDate getLastPlayedDate() {
        return lastPlayedDate;
    }

    public void setLastPlayedDate(LocalDate lastPlayedDate) {
        this.lastPlayedDate = lastPlayedDate;
    }

    public LocalDate getLastWinDate() {
        return lastWinDate;
    }

    public void setLastWinDate(LocalDate lastWinDate) {
        this.lastWinDate = lastWinDate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
