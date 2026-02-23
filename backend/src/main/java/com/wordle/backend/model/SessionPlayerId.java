package com.wordle.backend.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SessionPlayerId implements Serializable {
    private UUID sessionId;
    private Long userId;

    public SessionPlayerId() {}
    public SessionPlayerId(UUID sessionId, Long userId) {
        this.sessionId = sessionId;
        this.userId = userId;
    }
    // Getters, setters, equals, hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionPlayerId that = (SessionPlayerId) o;
        return Objects.equals(sessionId, that.sessionId) && Objects.equals(userId, that.userId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(sessionId, userId);
    }
}
