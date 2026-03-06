package com.wordle.backend.websocket.dto;

import java.util.UUID;

public class SubmitGuessRequest {
    private UUID gameId;
    private String guess;
    private String sessionCode;

    public SubmitGuessRequest() {}

    public SubmitGuessRequest(UUID gameId, String guess, String sessionCode) {
        this.gameId = gameId;
        this.guess = guess;
        this.sessionCode = sessionCode;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public String getGuess() {
        return guess;
    }

    public void setGuess(String guess) {
        this.guess = guess;
    }

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }
}
