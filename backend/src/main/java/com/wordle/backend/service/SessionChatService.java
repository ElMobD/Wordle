package com.wordle.backend.service;

import com.wordle.backend.model.SessionChat;
import com.wordle.backend.repository.SessionChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SessionChatService {
    @Autowired
    private SessionChatRepository sessionChatRepository;

    public SessionChat saveMessage(SessionChat chat) {
        return sessionChatRepository.save(chat);
    }

    public List<SessionChat> getMessagesBySession(UUID sessionId) {
        return sessionChatRepository.findBySessionId(sessionId);
    }
}
