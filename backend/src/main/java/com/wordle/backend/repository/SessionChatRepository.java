package com.wordle.backend.repository;

import com.wordle.backend.model.SessionChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionChatRepository extends JpaRepository<SessionChat, Long> {
    List<SessionChat> findBySessionId(java.util.UUID sessionId);
}
