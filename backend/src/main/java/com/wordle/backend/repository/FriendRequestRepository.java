package com.wordle.backend.repository;

import com.wordle.backend.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    
    List<FriendRequest> findByReceiverId(Long receiverId);
    
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, FriendRequest.FriendRequestStatus status);
    
    List<FriendRequest> findByRequesterId(Long requesterId);
    
    List<FriendRequest> findByRequesterIdAndStatus(Long requesterId, FriendRequest.FriendRequestStatus status);
    
    Optional<FriendRequest> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);
    
    @Query("SELECT fr FROM FriendRequest fr WHERE " +
           "((fr.requester.id = :userId1 AND fr.receiver.id = :userId2) OR " +
           "(fr.requester.id = :userId2 AND fr.receiver.id = :userId1))")
    List<FriendRequest> findAllBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
