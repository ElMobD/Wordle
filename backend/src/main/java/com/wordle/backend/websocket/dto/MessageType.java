package com.wordle.backend.websocket.dto;

public enum MessageType {
    CREATE,
    JOIN,
    CHAT,
    LOBBYINFOS,
    LEAVE_LOBBY,
    START_GAME,
    LOAD_GAME,
    PING,
    GET_CHAT_HISTORY
}
