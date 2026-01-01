package com.wordle.backend.service;

import com.wordle.backend.model.User;
import com.wordle.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Trouve un utilisateur par email ou le crée s'il n'existe pas
     * Utilisé lors de la connexion OAuth2 Google
     */
    public User findOrCreateUser(String googleId, String email, String name, String picture) {
        // Cherche d'abord par email
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            // L'utilisateur existe déjà, on le met à jour (au cas où nom/photo ont changé)
            User user = existingUser.get();
            user.setName(name);
            user.setPicture(picture);
            user.setGoogleId(googleId);
            return userRepository.save(user);
        }
        
        // L'utilisateur n'existe pas, on le crée
        User newUser = new User(googleId, email, name, picture);
        return userRepository.save(newUser);
    }

    /**
     * Trouve un utilisateur par email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Trouve un utilisateur par ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
