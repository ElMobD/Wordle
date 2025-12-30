package com.wordle.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/user/info")
    public Map<String, Object> userInfo(@AuthenticationPrincipal OAuth2User principal) {
        return Map.of(
            "message", "Voici les infos utilisateur",
            "attributes", principal.getAttributes()
        );
    }
}
