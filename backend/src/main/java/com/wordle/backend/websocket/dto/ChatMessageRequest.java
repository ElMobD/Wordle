package com.wordle.backend.websocket.dto;

public class ChatMessageRequest implements LobbyRequest {

    private MessageType type;
    private String sessionCode;
    private String message;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
