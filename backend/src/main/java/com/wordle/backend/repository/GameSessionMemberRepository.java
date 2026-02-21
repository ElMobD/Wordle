package com.wordle.backend.repository;

import com.wordle.backend.model.GameSessionMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameSessionMemberRepository extends JpaRepository<GameSessionMember, Long> {

    @Query("SELECT m FROM GameSessionMember m JOIN FETCH m.user WHERE m.session.id = :sessionId")
    List<GameSessionMember> findBySessionId(@Param("sessionId") Long sessionId);

    Optional<GameSessionMember> findBySessionIdAndUserId(Long sessionId, Long userId);
}
