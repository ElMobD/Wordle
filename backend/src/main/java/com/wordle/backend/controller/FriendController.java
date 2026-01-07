package com.wordle.backend.controller;

import com.wordle.backend.model.FriendRequest;
import com.wordle.backend.model.Friendship;
import com.wordle.backend.model.User;
import com.wordle.backend.repository.UserRepository;
import com.wordle.backend.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserRepository userRepository;

    // Liste des amis de l'utilisateur connecté
    @GetMapping("/list")
    public ResponseEntity<List<User>> getFriendsList(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<User> friends = friendService.getFriends(userId);
        return ResponseEntity.ok(friends);
    }

    // Demandes d'amitié en attente reçues
    @GetMapping("/requests/pending")
    public ResponseEntity<List<FriendRequest>> getPendingRequests(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<FriendRequest> requests = friendService.getPendingFriendRequests(userId);
        return ResponseEntity.ok(requests);
    }

    // Demandes envoyées
    @GetMapping("/requests/sent")
    public ResponseEntity<List<FriendRequest>> getSentRequests(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<FriendRequest> requests = friendService.getSentFriendRequests(userId);
        return ResponseEntity.ok(requests);
    }

    // Envoyer une demande d'amitié
    @PostMapping("/request/send/{receiverId}")
    public ResponseEntity<?> sendFriendRequest(@PathVariable Long receiverId, Authentication authentication) {
        try {
            Long requesterId = getUserId(authentication);
            FriendRequest friendRequest = friendService.sendFriendRequest(requesterId, receiverId);
            return ResponseEntity.ok(friendRequest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Accepter une demande d'amitié
    @PostMapping("/request/{requestId}/accept")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long requestId, Authentication authentication) {
        try {
            Long userId = getUserId(authentication);
            Friendship friendship = friendService.acceptFriendRequest(requestId, userId);
            return ResponseEntity.ok(friendship);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Rejeter une demande d'amitié
    @PostMapping("/request/{requestId}/reject")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable Long requestId, Authentication authentication) {
        try {
            Long userId = getUserId(authentication);
            friendService.rejectFriendRequest(requestId, userId);
            return ResponseEntity.ok(Map.of("message", "Demande rejetée"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Supprimer un ami
    @DeleteMapping("/remove/{friendId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long friendId, Authentication authentication) {
        try {
            Long userId = getUserId(authentication);
            friendService.removeFriend(userId, friendId);
            return ResponseEntity.ok(Map.of("message", "Ami supprimé"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Rechercher des utilisateurs par nom ou email
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam("query") String query, Authentication authentication) {
        Long userId = getUserId(authentication);
        List<User> results = friendService.searchUsers(userId, query);
        return ResponseEntity.ok(results);
    }

    // Vérifier si un utilisateur est ami
    @GetMapping("/status/{friendId}")
    public ResponseEntity<Map<String, Object>> checkFriendStatus(@PathVariable Long friendId, Authentication authentication) {
        Long userId = getUserId(authentication);
        boolean areFriends = friendService.areFriends(userId, friendId);
        Map<String, Object> response = new HashMap<>();
        response.put("areFriends", areFriends);
        return ResponseEntity.ok(response);
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Non authentifié");
        }
        String email = (String) authentication.getPrincipal();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return user.getId();
    }
}
