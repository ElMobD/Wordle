package com.wordle.backend.repository;

import com.wordle.backend.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.user1.id = :userId AND f.user2.id = :friendId) OR " +
           "(f.user1.id = :friendId AND f.user2.id = :userId)")
    Optional<Friendship> findBetweenUsers(@Param("userId") Long userId, @Param("friendId") Long friendId);
    
    @Query("SELECT f FROM Friendship f WHERE f.user1.id = :userId OR f.user2.id = :userId")
    List<Friendship> findFriendships(@Param("userId") Long userId);
}
