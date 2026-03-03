package com.wordle.backend.repository;

import com.wordle.backend.model.SessionChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionChatRepository extends JpaRepository<SessionChat, Long> {
    List<SessionChat> findBySessionId(java.util.UUID sessionId);

    @Query("SELECT sc FROM SessionChat sc JOIN FETCH sc.user WHERE sc.session.id = :sessionId ORDER BY sc.sentAt ASC")
    List<SessionChat> findBySessionIdWithUser(@Param("sessionId") java.util.UUID sessionId);
}
