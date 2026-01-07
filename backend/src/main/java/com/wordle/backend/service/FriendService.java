package com.wordle.backend.service;

import com.wordle.backend.model.FriendRequest;
import com.wordle.backend.model.Friendship;
import com.wordle.backend.model.User;
import com.wordle.backend.repository.FriendRequestRepository;
import com.wordle.backend.repository.FriendshipRepository;
import com.wordle.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FriendService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    // Envoyer une demande d'amitié
    public FriendRequest sendFriendRequest(Long requesterId, Long receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous envoyer une demande d'amitié");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Utilisateur demandeur non trouvé"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Utilisateur destinataire non trouvé"));

        // Déjà une demande existante dans un sens ou l'autre
        Optional<FriendRequest> existing = friendRequestRepository.findBetweenUsers(requesterId, receiverId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Une demande d'amitié existe déjà entre ces utilisateurs");
        }

        // Déjà amis
        Optional<Friendship> friendship = friendshipRepository.findBetweenUsers(requesterId, receiverId);
        if (friendship.isPresent()) {
            throw new IllegalArgumentException("Vous êtes déjà amis");
        }

        FriendRequest friendRequest = new FriendRequest(requester, receiver);
        return friendRequestRepository.save(friendRequest);
    }

    // Accepter une demande d'amitié
    public Friendship acceptFriendRequest(Long requestId, Long userId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande d'amitié non trouvée"));

        if (!friendRequest.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("Vous ne pouvez pas accepter cette demande");
        }

        friendRequest.setStatus(FriendRequest.FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(friendRequest);

        Friendship friendship = new Friendship(friendRequest.getRequester(), friendRequest.getReceiver());
        return friendshipRepository.save(friendship);
    }

    // Rejeter une demande d'amitié
    public void rejectFriendRequest(Long requestId, Long userId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande d'amitié non trouvée"));

        if (!friendRequest.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("Vous ne pouvez pas rejeter cette demande");
        }

        friendRequest.setStatus(FriendRequest.FriendRequestStatus.REJECTED);
        friendRequestRepository.save(friendRequest);
    }

    // Supprimer un ami
    public void removeFriend(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findBetweenUsers(userId, friendId)
                .orElseThrow(() -> new RuntimeException("Amitié non trouvée"));
        friendshipRepository.delete(friendship);
    }

    // Liste des amis d'un utilisateur
    public List<User> getFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        List<Friendship> friendships = friendshipRepository.findFriendships(userId);
        return friendships.stream()
                .map(f -> f.getOtherUser(user))
                .collect(Collectors.toList());
    }

    // Demandes en attente reçues
    public List<FriendRequest> getPendingFriendRequests(Long userId) {
        return friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequest.FriendRequestStatus.PENDING);
    }

    // Demandes envoyées
    public List<FriendRequest> getSentFriendRequests(Long userId) {
        return friendRequestRepository.findByRequesterId(userId);
    }

    // Vérifier si deux utilisateurs sont amis
    public boolean areFriends(Long userId1, Long userId2) {
        return friendshipRepository.findBetweenUsers(userId1, userId2).isPresent();
    }
}
