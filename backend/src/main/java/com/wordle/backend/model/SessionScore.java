package com.wordle.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "session_scores")
public class SessionScore {
    
    @EmbeddedId
    private SessionScoreId id;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 0;

    @Column(name = "rounds_won", nullable = false)
    private Integer roundsWon = 0;

    @Column(name = "rounds_lost", nullable = false)
    private Integer roundsLost = 0;

    @Column(name = "average_attempts", precision = 4, scale = 2)
    private BigDecimal averageAttempts;

    @Column(name = "total_time_remaining")
    private Integer totalTimeRemaining = 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructeurs
    public SessionScore() {
    }

    public SessionScore(UUID sessionId, Long userId) {
        this.id = new SessionScoreId(sessionId, userId);
        this.totalScore = 0;
        this.roundsWon = 0;
        this.roundsLost = 0;
        this.totalTimeRemaining = 0;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters et Setters
    public SessionScoreId getId() {
        return id;
    }

    public void setId(SessionScoreId id) {
        this.id = id;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getRoundsWon() {
        return roundsWon;
    }

    public void setRoundsWon(Integer roundsWon) {
        this.roundsWon = roundsWon;
    }

    public Integer getRoundsLost() {
        return roundsLost;
    }

    public void setRoundsLost(Integer roundsLost) {
        this.roundsLost = roundsLost;
    }

    public BigDecimal getAverageAttempts() {
        return averageAttempts;
    }

    public void setAverageAttempts(BigDecimal averageAttempts) {
        this.averageAttempts = averageAttempts;
    }

    public Integer getTotalTimeRemaining() {
        return totalTimeRemaining;
    }

    public void setTotalTimeRemaining(Integer totalTimeRemaining) {
        this.totalTimeRemaining = totalTimeRemaining;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Méthodes utilitaires
    public void addRoundScore(Integer roundScore, Integer attemptsUsed, Integer timeRemaining, boolean won) {
        this.totalScore += roundScore;
        this.totalTimeRemaining += timeRemaining;
        
        if (won) {
            this.roundsWon++;
        } else {
            this.roundsLost++;
        }
        
        // Recalculer la moyenne des tentatives
        int totalRounds = this.roundsWon + this.roundsLost;
        if (totalRounds > 0) {
            BigDecimal currentTotal = this.averageAttempts != null 
                ? this.averageAttempts.multiply(BigDecimal.valueOf(totalRounds - 1))
                : BigDecimal.ZERO;
            this.averageAttempts = currentTotal.add(BigDecimal.valueOf(attemptsUsed))
                .divide(BigDecimal.valueOf(totalRounds), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        this.updatedAt = LocalDateTime.now();
    }
}
