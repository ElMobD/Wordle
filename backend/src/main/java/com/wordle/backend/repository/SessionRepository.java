
package com.wordle.backend.repository;

import com.wordle.backend.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByCode(String code);

    boolean existsByHostIdAndStatusNotIn(Long hostId, java.util.List<String> statuses);
    // Récupère la session active d'un utilisateur (statut != FINISHED/CANCELLED)
    Optional<Session> findFirstByHostIdAndStatusNotIn(Long hostId, java.util.List<String> statuses);
}
