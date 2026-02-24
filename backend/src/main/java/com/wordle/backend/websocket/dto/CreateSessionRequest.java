package com.wordle.backend.websocket.dto;

public class CreateSessionRequest implements LobbyRequest {

    private MessageType type;
    private int rounds = 5;
    private int timeLimit = 60;
    private int wordLength = 5;

    @Override
    public MessageType getType() {
        return type;
    }

    // getters/setters
    public void setType(MessageType type) {
        this.type = type;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getWordLength() {
        return wordLength;
    }

    public void setWordLength(int wordLength) {
        this.wordLength = wordLength;
    }
}
