package com.wordle.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private SimpleWebSocketHandler simpleWebSocketHandler;

    @Autowired
    private com.wordle.backend.websocket.LobbyWebSocketHandler lobbyWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(simpleWebSocketHandler, "/socket").setAllowedOrigins("*");
        registry.addHandler(lobbyWebSocketHandler, "/ws/lobby").setAllowedOrigins("*");
    }
}