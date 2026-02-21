package com.wordle.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false)
    private String word;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameSessionStatus status;

    @Column(name = "word_length", nullable = false)
    private Integer wordLength;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts;

    @Column(name = "round_time_seconds", nullable = false)
    private Integer roundTimeSeconds;

    @Column(name = "total_rounds", nullable = false)
    private Integer totalRounds;

    @Column(name = "current_round", nullable = false)
    private Integer currentRound;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public GameSession() {
    }

    public GameSession(String code, User host, String word) {
        this.code = code;
        this.host = host;
        this.word = word;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = GameSessionStatus.LOBBY;
        }
        if (wordLength == null) {
            wordLength = 5;
        }
        if (maxAttempts == null) {
            maxAttempts = 6;
        }
        if (roundTimeSeconds == null) {
            roundTimeSeconds = 120;
        }
        if (totalRounds == null) {
            totalRounds = 1;
        }
        if (currentRound == null) {
            currentRound = 0;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public GameSessionStatus getStatus() {
        return status;
    }

    public void setStatus(GameSessionStatus status) {
        this.status = status;
    }

    public Integer getWordLength() {
        return wordLength;
    }

    public void setWordLength(Integer wordLength) {
        this.wordLength = wordLength;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Integer getRoundTimeSeconds() {
        return roundTimeSeconds;
    }

    public void setRoundTimeSeconds(Integer roundTimeSeconds) {
        this.roundTimeSeconds = roundTimeSeconds;
    }

    public Integer getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(Integer totalRounds) {
        this.totalRounds = totalRounds;
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public enum GameSessionStatus {
        LOBBY,
        IN_PROGRESS,
        ENDED
    }
}
