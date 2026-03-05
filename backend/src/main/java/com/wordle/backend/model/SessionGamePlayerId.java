package com.wordle.backend.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SessionGamePlayerId implements Serializable {
    
    private UUID sessionId;
    private UUID gameId;
    private Long userId;

    // Constructeurs
    public SessionGamePlayerId() {
    }

    public SessionGamePlayerId(UUID sessionId, UUID gameId, Long userId) {
        this.sessionId = sessionId;
        this.gameId = gameId;
        this.userId = userId;
    }

    // Getters et Setters
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionGamePlayerId that = (SessionGamePlayerId) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(gameId, that.gameId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, gameId, userId);
    }
}
