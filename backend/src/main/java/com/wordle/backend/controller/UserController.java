package com.wordle.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/")
    public String home() {
        return "Bienvenue ! <a href='/oauth2/authorization/google'>Se connecter avec Google</a>";
    }

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        return Map.of(
            "message", "Voici tout ce que Google a renvoyé",
            "attributes", principal.getAttributes()
        );
    }
}
