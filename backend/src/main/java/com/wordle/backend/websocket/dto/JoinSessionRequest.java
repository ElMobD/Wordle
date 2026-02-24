package com.wordle.backend.websocket.dto;

public class JoinSessionRequest implements LobbyRequest {

    private MessageType type;
    private String sessionCode;

    @Override
    public MessageType getType() {
        return type;
    }

    // getters/setters
    public void setType(MessageType type) {
        this.type = type;
    }

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }
}
