package com.wordle.backend.repository;

import com.wordle.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring génère automatiquement la requête :
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
    
    // Spring génère automatiquement la requête :
    // SELECT * FROM users WHERE google_id = ?
    Optional<User> findByGoogleId(String googleId);
}
