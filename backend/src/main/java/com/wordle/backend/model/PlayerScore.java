package com.wordle.backend.model;

public class PlayerScore {
    private Long userId;
    private String username;
    private String email;
    private Integer totalScore;
    private Integer wins;
    private Integer losses;
    private Double averageAttempts;

    public PlayerScore() {
    }

    public PlayerScore(Long userId, String username, String email, Integer totalScore, Integer wins, Integer losses, Double averageAttempts) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.totalScore = totalScore;
        this.wins = wins;
        this.losses = losses;
        this.averageAttempts = averageAttempts;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public Integer getLosses() {
        return losses;
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
    }

    public Double getAverageAttempts() {
        return averageAttempts;
    }

    public void setAverageAttempts(Double averageAttempts) {
        this.averageAttempts = averageAttempts;
    }
}
