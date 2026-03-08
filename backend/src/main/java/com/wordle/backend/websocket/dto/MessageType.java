package com.wordle.backend.websocket.dto;

public enum MessageType {
    CREATE,
    JOIN,
    CHAT,
    LOBBYINFOS,
    LEAVE_LOBBY,
    START_GAME,
    NEXT_ROUND,
    LOAD_GAME,
    SUBMIT_GUESS,
    PING,
    GET_CHAT_HISTORY,
    SHOW_LEADERBOARD
}
