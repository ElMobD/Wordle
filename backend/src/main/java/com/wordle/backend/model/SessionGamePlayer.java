package com.wordle.backend.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "session_game_player")
public class SessionGamePlayer {
    
    @EmbeddedId
    private SessionGamePlayerId id;

    @Column(nullable = false)
    private String status = "IN_PROGRESS";

    @Column(nullable = true)
    private Integer score = 0;

    // Constructeurs
    public SessionGamePlayer() {
    }

    public SessionGamePlayer(UUID sessionId, UUID gameId, Long userId) {
        this.id = new SessionGamePlayerId(sessionId, gameId, userId);
        this.status = "IN_PROGRESS";
        this.score = 0;
    }

    public SessionGamePlayer(UUID sessionId, UUID gameId, Long userId, String status, Integer score) {
        this.id = new SessionGamePlayerId(sessionId, gameId, userId);
        this.status = status;
        this.score = score;
    }

    // Getters et Setters
    public SessionGamePlayerId getId() {
        return id;
    }

    public void setId(SessionGamePlayerId id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
